#!/usr/bin/env python3
"""
Leafresh API 문서 업데이트 스크립트
Spring Boot 애플리케이션에서 OpenAPI JSON을 가져와 YAML로 변환합니다.
"""

import json
import yaml
import requests
import sys
from pathlib import Path

def fetch_openapi_from_server(url):
    """서버에서 OpenAPI JSON 스펙을 가져옵니다."""
    try:
        headers = {'Accept': 'application/json'}
        response = requests.get(url, headers=headers, timeout=30)
        response.raise_for_status()
        
        data = response.json()
        
        # 기본 검증
        if not data.get('openapi'):
            raise ValueError("Invalid OpenAPI format: missing 'openapi' field")
        
        if not data.get('info'):
            raise ValueError("Invalid OpenAPI format: missing 'info' field")
            
        if not data.get('paths'):
            raise ValueError("No API paths found in OpenAPI specification")
        
        return data
        
    except requests.exceptions.RequestException as e:
        raise Exception(f"Failed to fetch OpenAPI from server: {e}")
    except json.JSONDecodeError as e:
        raise Exception(f"Invalid JSON response from server: {e}")

def enhance_openapi_info(data):
    """OpenAPI 스펙의 info 섹션을 향상시킵니다."""
    
    # 기본 정보 업데이트
    info = data.setdefault('info', {})
    info['title'] = 'Leafresh API Documentation'
    info['description'] = """
환경보호를 실천하는 사람들을 위한 챌린지 플랫폼 Leafresh의 백엔드 API 문서입니다.

## 🌱 Leafresh 소개
Leafresh는 개인과 그룹이 함께 지속가능한 생활습관을 만들어가며, 
AI 기술로 활동을 검증하고 보상을 제공하는 환경보호 챌린지 플랫폼입니다.

## 🔐 인증 방법
대부분의 API는 JWT 토큰 인증이 필요합니다:
```
Authorization: Bearer YOUR_JWT_TOKEN
```

## 🌍 서버 환경
- 개발: https://springboot.dev-leafresh.app
- 프로덕션: https://api.leafresh.app
    """.strip()
    
    info['version'] = '1.0.0'
    info['contact'] = {
        'name': 'Leafresh Team',
        'url': 'https://leafresh.app'
    }
    
    # 서버 정보 업데이트
    data['servers'] = [
        {
            'url': 'https://api.leafresh.app',
            'description': '프로덕션 서버'
        },
        {
            'url': 'https://springboot.dev-leafresh.app',
            'description': '개발 서버'
        }
    ]
    
    return data

def save_as_yaml(data, output_file):
    """데이터를 YAML 파일로 저장합니다."""
    try:
        with open(output_file, 'w', encoding='utf-8') as f:
            yaml.dump(data, f, default_flow_style=False, allow_unicode=True, sort_keys=False)
        
        print(f"✅ OpenAPI YAML saved to {output_file}")
        print(f"📊 API paths: {len(data.get('paths', {}))}")
        print(f"📋 API title: {data['info'].get('title', 'Unknown')}")
        print(f"📌 API version: {data['info'].get('version', 'Unknown')}")
        
    except Exception as e:
        raise Exception(f"Failed to save YAML file: {e}")

def main():
    if len(sys.argv) < 2:
        print("Usage: python3 update-swagger-yaml.py <openapi-json-url> [output-file]")
        print("Example: python3 update-swagger-yaml.py http://localhost:8080/v3/api-docs")
        sys.exit(1)
    
    openapi_url = sys.argv[1]
    output_file = sys.argv[2] if len(sys.argv) > 2 else "swagger.yaml"
    
    try:
        print(f"🔍 Fetching OpenAPI spec from: {openapi_url}")
        data = fetch_openapi_from_server(openapi_url)
        
        print("🔧 Enhancing OpenAPI specification...")
        enhanced_data = enhance_openapi_info(data)
        
        print(f"💾 Saving to: {output_file}")
        save_as_yaml(enhanced_data, output_file)
        
        print("🎉 OpenAPI YAML update completed successfully!")
        
    except Exception as e:
        print(f"❌ Error: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
