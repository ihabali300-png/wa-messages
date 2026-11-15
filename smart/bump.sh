#!/bin/sh
# Usage: ./smart/bump.sh صغير "ملاحظة"   (الحجم: صغير / متوسط / كبير)
set -e
cd "$(dirname "$0")"
v=$(python3 -c "import json;print(json.load(open('version.json'))['v']+1)")
sed -i "s/const SMART_V = [0-9]*;/const SMART_V = $v;/" index.html
python3 - "$v" "$@" <<'PY'
import json, sys
json.dump({"v": int(sys.argv[1]), "size": sys.argv[2], "notes": sys.argv[3:]}, open("version.json", "w"), ensure_ascii=False)
PY
echo "smart version $v"
