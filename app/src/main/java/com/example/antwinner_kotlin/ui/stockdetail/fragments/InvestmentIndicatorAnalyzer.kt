 package com.example.antwinner_kotlin.ui.stockdetail.fragments

import kotlin.math.abs

/**
 * 투자 지표 분석기 - 각 지표를 분석하고 코멘트를 생성합니다
 */
object InvestmentIndicatorAnalyzer {

    /**
     * PER 분석
     */
    fun analyzePER(per: String?, industryAvgPer: Double?): IndicatorAnalysis {
        val perValue = parseNumber(per)
        val industryAvg = industryAvgPer ?: 15.0
        
        return when {
            perValue == null || perValue < 0 -> {
                IndicatorAnalysis(
                    value = per ?: "정보없음",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}배",
                    progressPercent = 0,
                    comment = "손실 상태로 PER 산정이 어렵습니다.",
                    rating = IndicatorRating.BAD
                )
            }
            perValue < 5 -> {
                IndicatorAnalysis(
                    value = "${String.format("%.1f", perValue)}배",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}배",
                    progressPercent = 20,
                    comment = "매우 저평가된 상태입니다. 투자 기회로 볼 수 있습니다.",
                    rating = IndicatorRating.EXCELLENT
                )
            }
            perValue < 10 -> {
                IndicatorAnalysis(
                    value = "${String.format("%.1f", perValue)}배",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}배",
                    progressPercent = 40,
                    comment = "저평가된 수준으로 매력적인 투자 기회입니다.",
                    rating = IndicatorRating.GOOD
                )
            }
            perValue < 15 -> {
                IndicatorAnalysis(
                    value = "${String.format("%.1f", perValue)}배",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}배",
                    progressPercent = 60,
                    comment = "적정한 수준의 평가를 받고 있습니다.",
                    rating = IndicatorRating.AVERAGE
                )
            }
            perValue < 25 -> {
                IndicatorAnalysis(
                    value = "${String.format("%.1f", perValue)}배",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}배",
                    progressPercent = 80,
                    comment = "다소 높은 평가를 받고 있습니다.",
                    rating = IndicatorRating.POOR
                )
            }
            else -> {
                IndicatorAnalysis(
                    value = "${String.format("%.1f", perValue)}배",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}배",
                    progressPercent = 100,
                    comment = "고평가된 상태로 투자 시 주의가 필요합니다.",
                    rating = IndicatorRating.BAD
                )
            }
        }
    }

    /**
     * PBR 분석
     */
    fun analyzePBR(pbr: String?, industryAvgPbr: Double?): IndicatorAnalysis {
        val pbrValue = parseNumber(pbr)
        val industryAvg = industryAvgPbr ?: 1.5
        
        return when {
            pbrValue == null -> {
                IndicatorAnalysis(
                    value = pbr ?: "정보없음",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}배",
                    progressPercent = 50,
                    comment = "PBR 정보를 확인할 수 없습니다.",
                    rating = IndicatorRating.AVERAGE
                )
            }
            pbrValue < 0.5 -> {
                IndicatorAnalysis(
                    value = "${String.format("%.1f", pbrValue)}배",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}배",
                    progressPercent = 20,
                    comment = "매우 저평가된 자산가치입니다. 우수한 투자 기회입니다.",
                    rating = IndicatorRating.EXCELLENT
                )
            }
            pbrValue < 1.0 -> {
                IndicatorAnalysis(
                    value = "${String.format("%.1f", pbrValue)}배",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}배",
                    progressPercent = 40,
                    comment = "저평가된 자산가치로 매력적입니다.",
                    rating = IndicatorRating.GOOD
                )
            }
            pbrValue < 2.0 -> {
                IndicatorAnalysis(
                    value = "${String.format("%.1f", pbrValue)}배",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}배",
                    progressPercent = 60,
                    comment = "합리적인 자산 평가를 받고 있습니다.",
                    rating = IndicatorRating.AVERAGE
                )
            }
            pbrValue < 3.0 -> {
                IndicatorAnalysis(
                    value = "${String.format("%.1f", pbrValue)}배",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}배",
                    progressPercent = 80,
                    comment = "다소 높은 자산 평가를 받고 있습니다.",
                    rating = IndicatorRating.POOR
                )
            }
            else -> {
                IndicatorAnalysis(
                    value = "${String.format("%.1f", pbrValue)}배",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}배",
                    progressPercent = 100,
                    comment = "고평가된 자산가치로 투자 시 주의 필요합니다.",
                    rating = IndicatorRating.BAD
                )
            }
        }
    }

    /**
     * ROE 분석
     */
    fun analyzeROE(roe: String?, industryAvgRoe: Double?): IndicatorAnalysis {
        val roeValue = parseNumber(roe)
        val industryAvg = industryAvgRoe ?: 8.0
        
        return when {
            roeValue == null -> {
                IndicatorAnalysis(
                    value = roe ?: "정보없음",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}%",
                    progressPercent = 50,
                    comment = "ROE 정보를 확인할 수 없습니다.",
                    rating = IndicatorRating.AVERAGE
                )
            }
            roeValue < 0 -> {
                IndicatorAnalysis(
                    value = "${String.format("%.1f", roeValue)}%",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}%",
                    progressPercent = 0,
                    comment = "자기자본이익률이 마이너스입니다. 수익성이 부족합니다.",
                    rating = IndicatorRating.BAD
                )
            }
            roeValue < 5 -> {
                IndicatorAnalysis(
                    value = "${String.format("%.1f", roeValue)}%",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}%",
                    progressPercent = 30,
                    comment = "자기자본이익률이 낮습니다. 수익성 개선이 필요합니다.",
                    rating = IndicatorRating.POOR
                )
            }
            roeValue < 10 -> {
                IndicatorAnalysis(
                    value = "${String.format("%.1f", roeValue)}%",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}%",
                    progressPercent = 50,
                    comment = "보통 수준의 자기자본이익률입니다.",
                    rating = IndicatorRating.AVERAGE
                )
            }
            roeValue < 15 -> {
                IndicatorAnalysis(
                    value = "${String.format("%.1f", roeValue)}%",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}%",
                    progressPercent = 70,
                    comment = "양호한 자기자본이익률을 보이고 있습니다.",
                    rating = IndicatorRating.GOOD
                )
            }
            else -> {
                IndicatorAnalysis(
                    value = "${String.format("%.1f", roeValue)}%",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}%",
                    progressPercent = 90,
                    comment = "우수한 자기자본이익률로 수익성이 뛰어납니다.",
                    rating = IndicatorRating.EXCELLENT
                )
            }
        }
    }

    /**
     * EPS 분석 (주당순이익)
     */
    fun analyzeEPS(eps: String?, industryAvgEps: Double?): IndicatorAnalysis {
        val epsValue = parseNumber(eps?.replace(",", ""))
        val industryAvg = industryAvgEps ?: 1000.0
        
        return when {
            epsValue == null -> {
                IndicatorAnalysis(
                    value = eps ?: "정보없음",
                    industryAvg = "업종 평균 ${String.format("%.0f", industryAvg)}원",
                    progressPercent = 50,
                    comment = "EPS 정보를 확인할 수 없습니다.",
                    rating = IndicatorRating.AVERAGE
                )
            }
            epsValue < 0 -> {
                IndicatorAnalysis(
                    value = "${String.format("%.0f", epsValue)}원",
                    industryAvg = "업종 평균 ${String.format("%.0f", industryAvg)}원",
                    progressPercent = 0,
                    comment = "주당순이익이 마이너스입니다. 손실 상태입니다.",
                    rating = IndicatorRating.BAD
                )
            }
            epsValue < 500 -> {
                IndicatorAnalysis(
                    value = "${String.format("%.0f", epsValue)}원",
                    industryAvg = "업종 평균 ${String.format("%.0f", industryAvg)}원",
                    progressPercent = 30,
                    comment = "주당순이익이 낮은 수준입니다.",
                    rating = IndicatorRating.POOR
                )
            }
            epsValue < 1500 -> {
                IndicatorAnalysis(
                    value = "${String.format("%.0f", epsValue)}원",
                    industryAvg = "업종 평균 ${String.format("%.0f", industryAvg)}원",
                    progressPercent = 60,
                    comment = "보통 수준의 주당순이익입니다.",
                    rating = IndicatorRating.AVERAGE
                )
            }
            epsValue < 3000 -> {
                IndicatorAnalysis(
                    value = "${String.format("%.0f", epsValue)}원",
                    industryAvg = "업종 평균 ${String.format("%.0f", industryAvg)}원",
                    progressPercent = 80,
                    comment = "양호한 주당순이익을 기록하고 있습니다.",
                    rating = IndicatorRating.GOOD
                )
            }
            else -> {
                IndicatorAnalysis(
                    value = "${String.format("%.0f", epsValue)}원",
                    industryAvg = "업종 평균 ${String.format("%.0f", industryAvg)}원",
                    progressPercent = 95,
                    comment = "우수한 주당순이익으로 수익성이 뛰어납니다.",
                    rating = IndicatorRating.EXCELLENT
                )
            }
        }
    }

    /**
     * 매출성장률 분석
     */
    fun analyzeRevenueGrowthRate(revenueGrowthRate: String?, industryAvgRevenueGrowth: Double?): IndicatorAnalysis {
        val growthValue = parseNumber(revenueGrowthRate)
        val industryAvg = industryAvgRevenueGrowth ?: 5.0
        
        return when {
            growthValue == null -> {
                IndicatorAnalysis(
                    value = revenueGrowthRate ?: "정보없음",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}%",
                    progressPercent = 50,
                    comment = "매출성장률 정보를 확인할 수 없습니다.",
                    rating = IndicatorRating.AVERAGE
                )
            }
            growthValue < -10 -> {
                IndicatorAnalysis(
                    value = "${String.format("%.1f", growthValue)}%",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}%",
                    progressPercent = 10,
                    comment = "매출이 급격히 감소하고 있습니다. 사업 전망에 주의가 필요합니다.",
                    rating = IndicatorRating.BAD
                )
            }
            growthValue < 0 -> {
                IndicatorAnalysis(
                    value = "${String.format("%.1f", growthValue)}%",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}%",
                    progressPercent = 25,
                    comment = "매출이 감소하고 있어 성장성이 우려됩니다.",
                    rating = IndicatorRating.POOR
                )
            }
            growthValue < 5 -> {
                IndicatorAnalysis(
                    value = "${String.format("%.1f", growthValue)}%",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}%",
                    progressPercent = 40,
                    comment = "매출성장률이 낮습니다. 성장동력 확보가 필요합니다.",
                    rating = IndicatorRating.POOR
                )
            }
            growthValue < 15 -> {
                IndicatorAnalysis(
                    value = "${String.format("%.1f", growthValue)}%",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}%",
                    progressPercent = 60,
                    comment = "안정적인 매출성장을 보이고 있습니다.",
                    rating = IndicatorRating.AVERAGE
                )
            }
            growthValue < 30 -> {
                IndicatorAnalysis(
                    value = "${String.format("%.1f", growthValue)}%",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}%",
                    progressPercent = 80,
                    comment = "양호한 매출성장률을 기록하고 있습니다.",
                    rating = IndicatorRating.GOOD
                )
            }
            else -> {
                IndicatorAnalysis(
                    value = "${String.format("%.1f", growthValue)}%",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}%",
                    progressPercent = 95,
                    comment = "우수한 매출성장률로 높은 성장성을 보이고 있습니다.",
                    rating = IndicatorRating.EXCELLENT
                )
            }
        }
    }

    /**
     * 영업이익률 분석 (영업이익/매출액으로 계산)
     */
    fun analyzeOperatingMargin(operatingIncome: String?, revenue: String?, industryAvgOperatingMargin: Double?): IndicatorAnalysis {
        val operatingIncomeValue = parseNumber(operatingIncome?.replace(",", ""))
        val revenueValue = parseNumber(revenue?.replace(",", ""))
        val industryAvg = industryAvgOperatingMargin ?: 8.0
        
        val operatingMargin = if (operatingIncomeValue != null && revenueValue != null && revenueValue != 0.0) {
            (operatingIncomeValue / revenueValue) * 100
        } else null
        
        return when {
            operatingMargin == null -> {
                IndicatorAnalysis(
                    value = "정보없음",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}%",
                    progressPercent = 50,
                    comment = "영업이익률 정보를 확인할 수 없습니다.",
                    rating = IndicatorRating.AVERAGE
                )
            }
            operatingMargin < -5 -> {
                IndicatorAnalysis(
                    value = "${String.format("%.1f", operatingMargin)}%",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}%",
                    progressPercent = 5,
                    comment = "영업손실이 큰 상태입니다. 경영 효율성이 매우 부족합니다.",
                    rating = IndicatorRating.BAD
                )
            }
            operatingMargin < 0 -> {
                IndicatorAnalysis(
                    value = "${String.format("%.1f", operatingMargin)}%",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}%",
                    progressPercent = 20,
                    comment = "영업손실 상태입니다. 수익성 개선이 시급합니다.",
                    rating = IndicatorRating.BAD
                )
            }
            operatingMargin < 5 -> {
                IndicatorAnalysis(
                    value = "${String.format("%.1f", operatingMargin)}%",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}%",
                    progressPercent = 35,
                    comment = "영업이익률이 낮습니다. 경영 효율성 향상이 필요합니다.",
                    rating = IndicatorRating.POOR
                )
            }
            operatingMargin < 10 -> {
                IndicatorAnalysis(
                    value = "${String.format("%.1f", operatingMargin)}%",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}%",
                    progressPercent = 60,
                    comment = "보통 수준의 영업이익률입니다.",
                    rating = IndicatorRating.AVERAGE
                )
            }
            operatingMargin < 20 -> {
                IndicatorAnalysis(
                    value = "${String.format("%.1f", operatingMargin)}%",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}%",
                    progressPercent = 80,
                    comment = "양호한 영업이익률로 효율적인 경영을 하고 있습니다.",
                    rating = IndicatorRating.GOOD
                )
            }
            else -> {
                IndicatorAnalysis(
                    value = "${String.format("%.1f", operatingMargin)}%",
                    industryAvg = "업종 평균 ${String.format("%.1f", industryAvg)}%",
                    progressPercent = 95,
                    comment = "우수한 영업이익률로 뛰어난 수익성을 보이고 있습니다.",
                    rating = IndicatorRating.EXCELLENT
                )
            }
        }
    }

    /**
     * 종합 분석 - 각 지표를 종합하여 전체 평가를 생성
     */
    fun generateOverallAnalysis(
        perAnalysis: IndicatorAnalysis,
        pbrAnalysis: IndicatorAnalysis,
        roeAnalysis: IndicatorAnalysis,
        epsAnalysis: IndicatorAnalysis,
        revenueGrowthAnalysis: IndicatorAnalysis,
        operatingMarginAnalysis: IndicatorAnalysis,
        industryName: String?
    ): OverallAnalysis {
        
        // 각 지표의 점수를 계산 (1-5점)
        val perScore = getRatingScore(perAnalysis.rating)
        val pbrScore = getRatingScore(pbrAnalysis.rating)
        val roeScore = getRatingScore(roeAnalysis.rating)
        val epsScore = getRatingScore(epsAnalysis.rating)
        val revenueGrowthScore = getRatingScore(revenueGrowthAnalysis.rating)
        val operatingMarginScore = getRatingScore(operatingMarginAnalysis.rating)
        
        // 가중 평균 계산 (수익성과 성장성에 높은 가중치)
        val totalScore = (perScore * 0.15 + pbrScore * 0.15 + roeScore * 0.25 + epsScore * 0.15 + 
                         revenueGrowthScore * 0.15 + operatingMarginScore * 0.15)
        
        val overallRating = when {
            totalScore >= 4.0 -> IndicatorRating.EXCELLENT
            totalScore >= 3.0 -> IndicatorRating.GOOD
            totalScore >= 2.5 -> IndicatorRating.AVERAGE
            totalScore >= 2.0 -> IndicatorRating.POOR
            else -> IndicatorRating.BAD
        }
        
        val attractionScore = when (overallRating) {
            IndicatorRating.EXCELLENT -> 5
            IndicatorRating.GOOD -> 4
            IndicatorRating.AVERAGE -> 3
            IndicatorRating.POOR -> 2
            IndicatorRating.BAD -> 1
        }
        
        val comment = getRandomAnalysisComment(overallRating, industryName, perScore, roeScore, revenueGrowthScore)
        
        return OverallAnalysis(
            comment = comment,
            attractionScore = attractionScore,
            rating = overallRating
        )
    }

    /**
     * 다양한 종합 분석 코멘트 생성 (랜덤 선택)
     */
    private fun getRandomAnalysisComment(
        rating: IndicatorRating, 
        industryName: String?, 
        perScore: Double, 
        roeScore: Double, 
        revenueGrowthScore: Double
    ): String {
        val industry = industryName?.takeIf { it.isNotBlank() } ?: "해당 업종"
        
        return when (rating) {
            IndicatorRating.EXCELLENT -> {
                listOf(
                    "탁월한 재무성과! $industry 내에서 최상위 수준의 경쟁력을 보여주고 있습니다. 수익성과 성장성이 균형있게 발달된 우량 기업입니다.",
                    "투자자들이 주목할 만한 뛰어난 기업! 안정적인 수익 창출 능력과 지속적인 성장 동력을 동시에 갖춘 매력적인 투자처입니다.",
                    "$industry 섹터의 숨은 보석! 강력한 펀더멘털과 우수한 수익성으로 장기 투자 관점에서 높은 가치를 인정받고 있습니다.",
                    "재무지표 올킬! 모든 핵심 지표에서 업계 평균을 크게 웃도는 성과를 보이며, 투자 매력도가 최고 수준입니다.",
                    "완벽한 재무 포트폴리오! 수익성, 안정성, 성장성의 삼박자가 조화롭게 어우러진 프리미엄 투자 대상입니다."
                ).random()
            }
            IndicatorRating.GOOD -> {
                listOf(
                    "안정감 있는 우량주! $industry 내에서 견고한 기반을 다져가고 있으며, 꾸준한 성과 개선이 기대됩니다.",
                    "성장 잠재력이 돋보이는 기업! 현재 양호한 재무상태를 바탕으로 미래 가치 상승이 충분히 예상됩니다.",
                    "균형 잡힌 투자처! 리스크 대비 수익률이 합리적이며, 중장기 관점에서 안정적인 수익을 기대할 수 있습니다.",
                    "탄탄한 기업 체질! $industry 특성을 잘 활용하여 지속가능한 성장 기반을 구축하고 있는 신뢰할 만한 기업입니다.",
                    "주목받기 시작하는 기업! 핵심 재무지표들이 개선 추세를 보이며, 향후 더 큰 성장이 기대되는 유망주입니다."
                ).random()
            }
            IndicatorRating.AVERAGE -> {
                listOf(
                    "평균적인 성과의 기업입니다. $industry 내 경쟁력 강화를 위한 추가적인 성장 동력 확보가 필요한 시점입니다.",
                    "면밀한 분석이 필요한 구간! 일부 지표에서 개선 여지가 보이나, 전반적으로 신중한 접근이 권장됩니다.",
                    "혼재된 신호를 보이는 기업! 강점과 약점이 공존하므로 개별 사업부문별 심화 분석을 통한 판단이 필요합니다.",
                    "변곡점에 서 있는 기업! $industry 트렌드 변화에 따른 대응 능력이 향후 성과를 좌우할 핵심 요소로 보입니다.",
                    "기회와 위험이 혼재! 현재 지표로는 중립적 평가이나, 경영진의 전략적 선택에 따라 결과가 달라질 수 있습니다."
                ).random()
            }
            IndicatorRating.POOR -> {
                listOf(
                    "주의 깊은 관찰이 필요한 상황! $industry 내 경쟁 열세가 우려되며, 구조적 개선 방안 모색이 시급해 보입니다.",
                    "재무 체질 개선이 과제! 현재 지표들이 업계 평균을 하회하고 있어 경영진의 적극적인 개선 노력이 요구됩니다.",
                    "단기적 어려움이 예상되는 구간! 투자 시에는 리스크 관리를 철저히 하고, 장기적 회복 가능성을 면밀히 검토해야 합니다.",
                    "신중한 투자 판단 필요! $industry 특성상 회복 가능성은 있으나, 현 시점에서는 보수적 접근이 바람직합니다.",
                    "투자 타이밍 재고 권장! 핵심 지표들의 개선 신호가 명확해질 때까지 관망하는 것이 현명한 선택일 수 있습니다."
                ).random()
            }
            IndicatorRating.BAD -> {
                listOf(
                    "고위험 구간 진입! $industry 내에서도 상당한 경쟁 열세에 있으며, 근본적인 사업 구조 재편이 필요한 상황입니다.",
                    "적신호 점등! 대부분의 재무지표가 우려스러운 수준이므로, 투자 전 매우 신중한 리스크 평가가 필수입니다.",
                    "터닝포인트 대기 중! 현재는 투자 부적합 구간이나, 드라마틱한 변화 가능성도 배제할 수 없는 상황입니다.",
                    "구조조정 필요 신호! $industry 내 생존을 위해서는 획기적인 경영 혁신과 재무 구조 개선이 시급합니다.",
                    "투자 회피 권장 구간! 현 상황에서는 자본 보전이 우선이며, 명확한 회복 신호 확인 후 재검토가 바람직합니다."
                ).random()
            }
        }
    }

    /**
     * 문자열에서 숫자를 추출하는 유틸리티 함수
     */
    private fun parseNumber(value: String?): Double? {
        return try {
            value?.replace("%", "")
                ?.replace("배", "")
                ?.replace(",", "")
                ?.trim()
                ?.toDouble()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 평가 등급을 점수로 변환
     */
    private fun getRatingScore(rating: IndicatorRating): Double {
        return when (rating) {
            IndicatorRating.EXCELLENT -> 5.0
            IndicatorRating.GOOD -> 4.0
            IndicatorRating.AVERAGE -> 3.0
            IndicatorRating.POOR -> 2.0
            IndicatorRating.BAD -> 1.0
        }
    }
} 