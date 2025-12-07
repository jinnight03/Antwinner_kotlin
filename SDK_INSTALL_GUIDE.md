# 카카오 AdFit SDK 설치 가이드

## 현재 상황
- SDK가 Maven 저장소에서 자동으로 다운로드되지 않음
- 빌드는 성공하지만 SDK 관련 코드는 주석 처리됨
- 광고 단위 ID `DAN-i0idA4lyPWuhyvhd`는 이미 적용됨

## 해결 방법

### 방법 1: Android Studio에서 Gradle Sync 재시도

1. Android Studio에서 프로젝트 열기
2. **File → Sync Project with Gradle Files** 실행
3. 또는 상단의 **"Sync Now"** 버튼 클릭
4. SDK가 다운로드되면 코드 주석 해제

### 방법 2: SDK 수동 다운로드 및 설치

1. **SDK 다운로드**
   - [카카오 AdFit Android SDK GitHub](https://github.com/adfit/adfit-android-sdk) 방문
   - 또는 [AdFit 가이드](https://adfit.github.io/wiki/android-guide/) 참고
   - SDK `.aar` 파일 다운로드

2. **프로젝트에 추가**
   ```
   app/libs/
   └── adfit-android-sdk.aar (또는 해당 파일명)
   ```

3. **build.gradle.kts 수정**
   ```kotlin
   dependencies {
       // 기존 Maven 의존성 주석 처리
       // implementation("com.kakao.adfit:ads-base:3.11.10")
       
       // 수동 설치
       implementation(files("libs/adfit-android-sdk.aar"))
   }
   ```

4. **코드 활성화**
   - `HomeFragment.kt`에서 주석 해제:
     - Import 문 (86-87번째 줄)
     - `setupNativeAd(view)` 호출 (182번째 줄)
     - `setupNativeAd` 함수 (484-561번째 줄)

### 방법 3: 다른 Maven 저장소 URL 시도

`build.gradle`의 저장소 URL을 확인하고 필요시 변경:

```groovy
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
        // 카카오 AdFit SDK 저장소
        maven { url 'https://devrepo.kakao.com/nexus/content/groups/public/' }
    }
}
```

## 코드 활성화 방법

SDK가 설치되면 다음을 수행:

1. **Import 문 주석 해제** (`HomeFragment.kt` 86-87번째 줄):
   ```kotlin
   import com.kakao.adfit.ads.na.NativeAdLoader
   import com.kakao.adfit.ads.na.NativeAd
   ```

2. **함수 호출 활성화** (182번째 줄):
   ```kotlin
   setupNativeAd(view)
   ```

3. **함수 주석 해제** (484-561번째 줄):
   - `/* AdFit SDK 다운로드 후 주석 해제 필요` 제거
   - 함수 끝의 `*/` 제거

## 확인 사항

- ✅ 광고 단위 ID: `DAN-i0idA4lyPWuhyvhd` (적용 완료)
- ✅ AndroidManifest.xml: AdFitService 등록 완료
- ✅ 권한 설정: INTERNET, ACCESS_NETWORK_STATE, AD_ID 완료
- ⏳ SDK 다운로드: 진행 중

## 참고 자료

- [AdFit Android 가이드](https://adfit.github.io/wiki/android-guide/)
- [AdFit Android SDK GitHub](https://github.com/adfit/adfit-android-sdk)
- [AdFit 시작하기](https://adfit.kakao.com/web/html/use.html)

