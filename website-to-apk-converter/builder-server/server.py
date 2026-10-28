import os, re, shutil, subprocess, tempfile
from pathlib import Path
from fastapi import FastAPI, HTTPException
from fastapi.responses import FileResponse
from pydantic import BaseModel, HttpUrl

app = FastAPI(title="Website to APK Builder")
ROOT = Path(__file__).resolve().parent.parent
TEMPLATE = ROOT / "generated-app-template"
ANDROID_HOME = os.environ.get("ANDROID_SDK_ROOT") or os.environ.get("ANDROID_HOME")

class BuildRequest(BaseModel):
    url: HttpUrl
    app_name: str
    package_id: str = "com.example.websiteapp"
    overlay: bool = True

@app.get("/health")
def health():
    return {"ok": True, "android_sdk_configured": bool(ANDROID_HOME)}

@app.post("/build")
def build(req: BuildRequest):
    url = str(req.url)
    if not url.startswith("https://"):
        raise HTTPException(400, "Only HTTPS websites are supported")
    app_name = req.app_name.strip()
    if not app_name or len(app_name) > 40:
        raise HTTPException(400, "App name must be 1-40 characters")
    package_id = req.package_id.strip()
    if not re.fullmatch(r"[a-zA-Z][a-zA-Z0-9_]*(\.[a-zA-Z][a-zA-Z0-9_]*)+", package_id):
        raise HTTPException(400, "Invalid Android package ID")
    if not TEMPLATE.exists():
        raise HTTPException(500, "Generated app template is missing")
    work = Path(tempfile.mkdtemp(prefix="webapk-build-"))
    project = work / "project"
    shutil.copytree(TEMPLATE, project)
    gradle_file = project / "app/build.gradle"
    text = gradle_file.read_text(encoding="utf-8")
    text = text.replace('applicationId "com.webapk.generated"', f'applicationId "{package_id}"')
    gradle_file.write_text(text, encoding="utf-8")
    main_file = project / "app/src/main/java/com/webapk/generated/MainActivity.kt"
    source = main_file.read_text(encoding="utf-8")
    source = source.replace('https://example.com', url).replace('Website App', app_name)
    main_file.write_text(source, encoding="utf-8")
    manifest = project / "app/src/main/AndroidManifest.xml"
    m = manifest.read_text(encoding="utf-8")
    if not req.overlay:
        m = m.replace('<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />', '')
    manifest.write_text(m, encoding="utf-8")
    env = os.environ.copy()
    if ANDROID_HOME:
        env["ANDROID_SDK_ROOT"] = ANDROID_HOME
        env["ANDROID_HOME"] = ANDROID_HOME
    try:
        subprocess.run(["gradle", "assembleDebug", "--no-daemon"], cwd=project, env=env, check=True, capture_output=True, text=True, timeout=600)
    except subprocess.TimeoutExpired:
        shutil.rmtree(work, ignore_errors=True)
        raise HTTPException(504, "Build timed out")
    except subprocess.CalledProcessError as e:
        detail = (e.stderr or e.stdout or "Gradle build failed")[-5000:]
        shutil.rmtree(work, ignore_errors=True)
        raise HTTPException(500, detail)
    apk = project / "app/build/outputs/apk/debug/app-debug.apk"
    if not apk.exists():
        shutil.rmtree(work, ignore_errors=True)
        raise HTTPException(500, "Gradle finished but APK was not found")
    return FileResponse(apk, media_type="application/vnd.android.package-archive", filename=f"{re.sub(r'[^A-Za-z0-9_-]', '_', app_name)}-debug.apk", background=None)
