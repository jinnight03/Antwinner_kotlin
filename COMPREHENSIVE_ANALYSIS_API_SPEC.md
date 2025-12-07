# 종합분석 API 스펙 문서

## 개요
종목정보 화면의 "주요 투자 지표" 메뉴에서 사용되는 종합분석 API 스펙입니다.

## API 엔드포인트

### 기본 정보
- **Method**: `GET`
- **Base URL**: `https://antwinner.com/api/`
- **Endpoint**: `stock_comprehensive_analysis/{stockName}`
- **Full URL 예시**: `https://antwinner.com/api/stock_comprehensive_analysis/삼성전자`
- **Path Parameter**: 
  - `stockName` (String): 종목명 (URL 인코딩 없이 그대로 전달)

## 응답 구조

### 최상위 응답 객체 (ComprehensiveAnalysisResponse)

```json
{
  "success": true,
  "stock_name": "삼성전자",
  "stock_symbol": "005930",
  "industry": "반도체 제조업",
  "analysis_date": "2024-01-15",
  "comprehensive_analysis": { ... },
  "indicators": { ... }
}
```

#### 필드 설명

| 필드명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| `success` | Boolean | ✅ | API 호출 성공 여부 |
| `stock_name` | String | ❌ | 종목명 |
| `stock_symbol` | String | ❌ | 종목코드 |
| `industry` | String | ❌ | 업종명 |
| `analysis_date` | String | ❌ | 분석 일자 (YYYY-MM-DD 형식 권장) |
| `comprehensive_analysis` | Object | ✅ | 종합 분석 데이터 |
| `indicators` | Object | ✅ | 개별 지표 데이터 |

---

### 종합 분석 데이터 (comprehensive_analysis)

```json
{
  "total_score": 3.5,
  "overall_rating": "GOOD",
  "attraction_score": 4,
  "overall_comment": "이 종목은 전반적으로 양호한 투자 가치를 보여줍니다. ROE와 영업이익률이 업종 평균을 상회하며, 안정적인 성장세를 보이고 있습니다."
}
```

#### 필드 설명

| 필드명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| `total_score` | Double | ✅ | 종합 점수 (0.0 ~ 5.0) |
| `overall_rating` | String | ✅ | 전체 평가 등급 (EXCELLENT, GOOD, AVERAGE, POOR, BAD 중 하나) |
| `attraction_score` | Integer | ✅ | 투자 매력도 점수 (1 ~ 5) |
| `overall_comment` | String | ✅ | 종합 분석 코멘트 (한글 설명) |

#### overall_rating 값
- `"EXCELLENT"`: 우수 (5점)
- `"GOOD"`: 양호 (4점)
- `"AVERAGE"`: 보통 (3점)
- `"POOR"`: 저조 (2점)
- `"BAD"`: 불량 (1점)

---

### 개별 지표 데이터 (indicators)

```json
{
  "per": { ... },
  "pbr": { ... },
  "roe": { ... },
  "eps": { ... },
  "revenue_growth": { ... },
  "operating_margin": { ... }
}
```

#### 필드 설명

| 필드명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| `per` | Object | ✅ | PER (주가수익비율) 지표 |
| `pbr` | Object | ✅ | PBR (주가순자산비율) 지표 |
| `roe` | Object | ✅ | ROE (자기자본이익률) 지표 |
| `eps` | Object | ✅ | EPS (주당순이익) 지표 |
| `revenue_growth` | Object | ✅ | 매출 성장률 지표 |
| `operating_margin` | Object | ✅ | 영업이익률 지표 |

**참고**: 각 지표는 `IndicatorDetail` 객체이거나 `null`일 수 있습니다.

---

### 개별 지표 상세 정보 (IndicatorDetail)

```json
{
  "value": "17.4배",
  "raw_value": 17.4,
  "industry_avg": "업종 평균 19.6배",
  "industry_avg_raw": 19.6,
  "progress_percent": 75,
  "comment": "PER이 업종 평균보다 낮아 저평가되어 있습니다.",
  "rating": "GOOD",
  "score": 4.0
}
```

#### 필드 설명

| 필드명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| `value` | String | ✅ | 표시용 값 (예: "17.4배", "30.69%", "-1,953원") |
| `raw_value` | Double | ❌ | 원시 숫자 값 (계산용) |
| `industry_avg` | String | ✅ | 업종 평균 표시용 문자열 (예: "업종 평균 19.6배") |
| `industry_avg_raw` | Double | ❌ | 업종 평균 원시 숫자 값 |
| `progress_percent` | Integer | ✅ | 프로그레스 바 퍼센트 (0 ~ 100) |
| `comment` | String | ✅ | 해당 지표에 대한 분석 코멘트 |
| `rating` | String | ✅ | 평가 등급 (EXCELLENT, GOOD, AVERAGE, POOR, BAD) |
| `score` | Double | ❌ | 점수 (1.0 ~ 5.0) |

---

## 전체 응답 예시

```json
{
  "success": true,
  "stock_name": "삼성전자",
  "stock_symbol": "005930",
  "industry": "반도체 제조업",
  "analysis_date": "2024-01-15",
  "comprehensive_analysis": {
    "total_score": 3.5,
    "overall_rating": "GOOD",
    "attraction_score": 4,
    "overall_comment": "이 종목은 전반적으로 양호한 투자 가치를 보여줍니다. ROE와 영업이익률이 업종 평균을 상회하며, 안정적인 성장세를 보이고 있습니다."
  },
  "indicators": {
    "per": {
      "value": "17.4배",
      "raw_value": 17.4,
      "industry_avg": "업종 평균 19.6배",
      "industry_avg_raw": 19.6,
      "progress_percent": 75,
      "comment": "PER이 업종 평균보다 낮아 저평가되어 있습니다.",
      "rating": "GOOD",
      "score": 4.0
    },
    "pbr": {
      "value": "0.32배",
      "raw_value": 0.32,
      "industry_avg": "업종 평균 1.27배",
      "industry_avg_raw": 1.27,
      "progress_percent": 25,
      "comment": "PBR이 업종 평균보다 낮습니다.",
      "rating": "POOR",
      "score": 2.0
    },
    "roe": {
      "value": "15.70%",
      "raw_value": 15.70,
      "industry_avg": "업종 평균 4.62%",
      "industry_avg_raw": 4.62,
      "progress_percent": 90,
      "comment": "ROE가 업종 평균을 크게 상회하여 수익성이 우수합니다.",
      "rating": "EXCELLENT",
      "score": 5.0
    },
    "eps": {
      "value": "1,953원",
      "raw_value": 1953.0,
      "industry_avg": "업종 평균 1,436원",
      "industry_avg_raw": 1436.0,
      "progress_percent": 80,
      "comment": "EPS가 업종 평균보다 높아 수익성이 양호합니다.",
      "rating": "GOOD",
      "score": 4.0
    },
    "revenue_growth": {
      "value": "12.5%",
      "raw_value": 12.5,
      "industry_avg": "업종 평균 8.3%",
      "industry_avg_raw": 8.3,
      "progress_percent": 85,
      "comment": "매출 성장률이 업종 평균을 상회하여 성장성이 우수합니다.",
      "rating": "GOOD",
      "score": 4.0
    },
    "operating_margin": {
      "value": "18.5%",
      "raw_value": 18.5,
      "industry_avg": "업종 평균 12.3%",
      "industry_avg_raw": 12.3,
      "progress_percent": 88,
      "comment": "영업이익률이 업종 평균을 크게 상회하여 수익성이 우수합니다.",
      "rating": "EXCELLENT",
      "score": 5.0
    }
  }
}
```

---

## 에러 응답 예시

```json
{
  "success": false,
  "stock_name": null,
  "stock_symbol": null,
  "industry": null,
  "analysis_date": null,
  "comprehensive_analysis": null,
  "indicators": null
}
```

**참고**: `success: false`인 경우, 안드로이드 앱은 기존 방식(fallback)으로 데이터를 가져오려고 시도합니다.

---

## 구현 시 주의사항

### 1. 종목명 처리
- Path Parameter로 전달되는 `stockName`은 URL 인코딩하지 않고 그대로 사용합니다.
- 예: `stock_comprehensive_analysis/삼성전자` (인코딩 없음)

### 2. Rating 값
- 반드시 대문자로 전달해야 합니다: `"EXCELLENT"`, `"GOOD"`, `"AVERAGE"`, `"POOR"`, `"BAD"`
- 안드로이드 앱은 `.uppercase()`로 변환하여 처리하지만, 일관성을 위해 대문자로 전달하는 것을 권장합니다.

### 3. progress_percent 계산
- 프로그레스 바에 표시될 퍼센트 값 (0 ~ 100)
- 업종 평균 대비 종목의 상대적 위치를 나타냅니다.
- 예: 업종 평균보다 25% 높으면 `75`, 업종 평균과 동일하면 `50`

### 4. attraction_score 매핑
- `EXCELLENT` → 5
- `GOOD` → 4
- `AVERAGE` → 3
- `POOR` → 2
- `BAD` → 1

### 5. 필수 필드
- `success`, `comprehensive_analysis`, `indicators`는 필수입니다.
- 각 지표(`per`, `pbr`, `roe`, `eps`, `revenue_growth`, `operating_margin`)는 `null`일 수 있지만, 가능하면 모든 지표를 제공하는 것을 권장합니다.

### 6. 코멘트 작성 가이드
- 각 지표의 코멘트는 업종 평균과의 비교를 포함하는 것이 좋습니다.
- 종합 분석 코멘트는 전체적인 투자 의견을 포함해야 합니다.
- 한글로 작성하며, 사용자에게 이해하기 쉬운 표현을 사용합니다.

---

## 참고: 기존 분석 로직

안드로이드 앱에서 사용하는 기존 분석 로직의 가중치:

- PER: 15%
- PBR: 15%
- ROE: 25% (가장 높은 가중치)
- EPS: 15%
- 매출 성장률: 15%
- 영업이익률: 15%

종합 점수 계산:
```
total_score = (perScore * 0.15 + pbrScore * 0.15 + roeScore * 0.25 + 
               epsScore * 0.15 + revenueGrowthScore * 0.15 + operatingMarginScore * 0.15)
```

등급 판정:
- `total_score >= 4.0` → EXCELLENT
- `total_score >= 3.0` → GOOD
- `total_score >= 2.5` → AVERAGE
- `total_score >= 2.0` → POOR
- `total_score < 2.0` → BAD

서버에서도 동일한 로직을 사용하여 일관성을 유지하는 것을 권장합니다.

