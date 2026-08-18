#!/bin/bash
# 图标生成脚本
# 用法: ./scripts/generate-icons.sh <source.png>
# 要求: 源图片至少 1024x1024，正方形

set -e

SOURCE="${1:-src/main/resources/icon.png}"

if [ ! -f "$SOURCE" ]; then
    echo "错误: 找不到源图片 $SOURCE"
    echo "用法: ./scripts/generate-icons.sh <你的图标.png>"
    echo ""
    echo "图标要求:"
    echo "  - 正方形，至少 1024x1024 像素"
    echo "  - PNG 格式"
    echo "  - 建议使用透明背景"
    exit 1
fi

ICONSET="icon.iconset"
OUTPUT_DIR="src/main/resources"

echo "=== 生成 macOS .icns ==="
mkdir -p "$ICONSET"
sips -z 16 16     "$SOURCE" --out "$ICONSET/icon_16x16.png"
sips -z 32 32     "$SOURCE" --out "$ICONSET/icon_16x16@2x.png"
sips -z 32 32     "$SOURCE" --out "$ICONSET/icon_32x32.png"
sips -z 64 64     "$SOURCE" --out "$ICONSET/icon_32x32@2x.png"
sips -z 128 128   "$SOURCE" --out "$ICONSET/icon_128x128.png"
sips -z 256 256   "$SOURCE" --out "$ICONSET/icon_128x128@2x.png"
sips -z 256 256   "$SOURCE" --out "$ICONSET/icon_256x256.png"
sips -z 512 512   "$SOURCE" --out "$ICONSET/icon_256x256@2x.png"
sips -z 512 512   "$SOURCE" --out "$ICONSET/icon_512x512.png"
sips -z 1024 1024 "$SOURCE" --out "$ICONSET/icon_512x512@2x.png"
iconutil -c icns "$ICONSET" -o "$OUTPUT_DIR/icon.icns"
rm -rf "$ICONSET"
echo "  -> $OUTPUT_DIR/icon.icns"

echo "=== 生成 Windows .ico ==="
python3 -c "
from PIL import Image
img = Image.open('$SOURCE')
sizes = [16, 32, 48, 64, 128, 256]
img.save('$OUTPUT_DIR/icon.ico', format='ICO', sizes=[(s, s) for s in sizes])
" 2>/dev/null || {
    echo "  警告: 需要 Pillow 库来生成 .ico，请执行: pip3 install Pillow"
    echo "  暂时跳过 .ico 生成"
}
if [ -f "$OUTPUT_DIR/icon.ico" ]; then
    echo "  -> $OUTPUT_DIR/icon.ico"
fi

echo "=== 生成 Linux .png ==="
cp "$SOURCE" "$OUTPUT_DIR/icon.png" 2>/dev/null || true
echo "  -> $OUTPUT_DIR/icon.png"

echo ""
echo "=== 完成 ==="
echo "已生成以下图标文件:"
ls -la "$OUTPUT_DIR"/icon.* 2>/dev/null