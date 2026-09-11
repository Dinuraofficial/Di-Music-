import urllib.request
import json
import subprocess

vid = "4NRXx6U8ABQ"

# 1. Visitor data
req_v = urllib.request.Request("https://www.youtube.com/sw.js_data", headers={
    "User-Agent": "com.google.android.youtube/20.10.38 (Linux; U; ANDROID 11) gzip",
    "Accept": "application/json"
})
res_v = urllib.request.urlopen(req_v, timeout=5)
body = res_v.read().decode()
if body.startswith(")]}'"): body = body[4:]
vdata = json.loads(body)[0][2][0][0][13]
print("vdata obtained:", vdata[:20])

# 2. Generate poToken
token = subprocess.check_output([
    "node", "-e",
    f'import("/tmp/test-bgutils/node_modules/bgutils-js/dist/exports/webpo.js").then(w => console.log(w.createColdStartToken("{vid}")));'
]).decode().strip()
print("Generated poToken:", token)

# 3. Test player
payload = {
    "videoId": vid,
    "contentCheckOk": True,
    "racyCheckOk": True,
    "context": {
        "client": {
            "clientName": "WEB",
            "clientVersion": "2.20240901.01.00",
            "visitorData": vdata,
            "serviceIntegrityDimensions": {
                "poToken": token
            },
            "hl": "en",
            "gl": "US"
        }
    }
}

req_p = urllib.request.Request(
    "https://www.youtube.com/youtubei/v1/player",
    data=json.dumps(payload).encode(),
    headers={
        "Content-Type": "application/json",
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
    }
)
try:
    r = urllib.request.urlopen(req_p, timeout=5)
    d = json.loads(r.read().decode())
    ps = d.get("playabilityStatus", {})
    print("WEB with poToken status:", ps.get("status"), "reason:", ps.get("reason"))
    if "streamingData" in d:
        sd = d["streamingData"]
        formats = sd.get("adaptiveFormats", [])
        print("  Formats count:", len(formats))
        for f in formats:
            if f.get("mimeType", "").startswith("audio/"):
                print("  Audio format found! Cipher:", "signatureCipher" in f, "Url:", f.get("url", "NONE")[:60])
                break
except Exception as e:
    print("Failed:", e)
