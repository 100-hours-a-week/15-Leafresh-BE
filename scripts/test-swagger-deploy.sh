#!/bin/bash

# Swagger UI 배포 테스트 스크립트
# 로컬에서 Swagger UI Action을 테스트하기 위한 스크립트입니다.

set -e

echo "🔧 Swagger UI 배포 테스트를 시작합니다..."

# 1. 프로젝트 루트로 이동
cd "$(dirname "$0")/.."

# 2. 의존성 확인
if ! command -v docker &> /dev/null; then
    echo "❌ Docker가 설치되어 있지 않습니다."
    exit 1
fi

# 3. swagger.yaml 파일 존재 확인
if [ ! -f "swagger.yaml" ]; then
    echo "❌ swagger.yaml 파일이 존재하지 않습니다."
    exit 1
fi

echo "✅ swagger.yaml 파일을 확인했습니다."

# 4. Swagger UI 생성 (Docker를 사용하여 Legion2/swagger-ui-action과 동일한 결과)
echo "🔨 Swagger UI를 생성 중..."

# swagger-ui 폴더 생성
rm -rf swagger-ui
mkdir -p swagger-ui

# Swagger UI 최신 버전 다운로드 및 설정
SWAGGER_UI_VERSION="5.10.3"
curl -L "https://github.com/swagger-api/swagger-ui/archive/v${SWAGGER_UI_VERSION}.tar.gz" | tar xz
cp -r "swagger-ui-${SWAGGER_UI_VERSION}/dist/"* swagger-ui/
rm -rf "swagger-ui-${SWAGGER_UI_VERSION}"

# swagger.yaml을 swagger-ui 폴더에 복사
cp swagger.yaml swagger-ui/

# index.html 수정하여 우리의 swagger.yaml을 가리키도록 설정
cat > swagger-ui/index.html << 'EOF'
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8">
    <title>Leafresh API Documentation</title>
    <link rel="stylesheet" type="text/css" href="./swagger-ui.css" />
    <link rel="icon" type="image/png" href="./favicon-32x32.png" sizes="32x32" />
    <link rel="icon" type="image/png" href="./favicon-16x16.png" sizes="16x16" />
    <style>
      html {
        box-sizing: border-box;
        overflow: -moz-scrollbars-vertical;
        overflow-y: scroll;
      }

      *, *:before, *:after {
        box-sizing: inherit;
      }

      body {
        margin:0;
        background: #fafafa;
      }
    </style>
  </head>

  <body>
    <div id="swagger-ui"></div>

    <script src="./swagger-ui-bundle.js" charset="UTF-8"> </script>
    <script src="./swagger-ui-standalone-preset.js" charset="UTF-8"> </script>
    <script>
    window.onload = function() {
      const ui = SwaggerUIBundle({
        url: './swagger.yaml',
        dom_id: '#swagger-ui',
        deepLinking: true,
        presets: [
          SwaggerUIBundle.presets.apis,
          SwaggerUIStandalonePreset
        ],
        plugins: [
          SwaggerUIBundle.plugins.DownloadUrl
        ],
        layout: "StandaloneLayout"
      });
    };
    </script>
  </body>
</html>
EOF

echo "✅ Swagger UI가 생성되었습니다."

# 5. Python 간단한 HTTP 서버로 테스트
if command -v python3 &> /dev/null; then
    echo "🌐 로컬 서버를 시작합니다..."
    echo "📖 브라우저에서 http://localhost:8000 을 열어 확인하세요"
    echo "🛑 서버를 중지하려면 Ctrl+C를 누르세요"
    cd swagger-ui
    python3 -m http.server 8000
else
    echo "✅ swagger-ui 폴더가 생성되었습니다."
    echo "📁 생성된 파일들:"
    ls -la swagger-ui/
fi

echo "🎉 Swagger UI 배포 테스트 완료!"
