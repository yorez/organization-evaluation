# 360 다면평가 & MBO 실적평가
직원들 간에 관계를 설정한 뒤, 다면평가 또는 실적평가를 직접 운영할 수 있는 WEB 프로그램입니다. :-)

## Stack
![](https://img.shields.io/badge/springboot-%E2%9C%93-blue.svg) ![](https://img.shields.io/badge/jpa-%E2%9C%93-blue.svg) ![](https://img.shields.io/badge/querydsl-✓-blue.svg) ![](https://img.shields.io/badge/springwebsecurity-✓-blue.svg) ![](https://img.shields.io/badge/maven-%E2%9C%93-blue.svg) ![](https://img.shields.io/badge/mysql-%E2%9C%93-blue.svg) ![](https://img.shields.io/badge/thymeleaf-%E2%9C%93-blue.svg) ![](https://img.shields.io/badge/bootstrap-%E2%9C%93-blue.svg) ![](https://img.shields.io/badge/font--awesome-✓-blue.svg) ![](https://img.shields.io/badge/jquery-%E2%9C%93-blue.svg)

## 개요
### 다면평가
- 객관식 : 동일한 항목에 대한 본인과 타인의 평가를 비교해봅니다. 이로써, 자신이 생각하는 본인의 모습과 타인이 생각하는 본인의 모습 차이를 명확하게 인지하고 Feedback 받을 수 있습니다.
- 주관식 : 평소 구두로는 전달하기 어려웠던 고마움, 서운함, 등을 온라인을 통해 전달함으로써 조직 분위기 쇄신의 계기를 마련합니다. (ex> ooo의 강점은 무엇인가요?, ooo의 약점은 무엇인가요?, 평소 하고 싶었던 말을 자유롭게 기술해주세요. 등)
> 객관식, 주관식 모두 항목 수에 제한이 없습니다. <br>
> 객관식은 평가받을 척도 수, 내용, 점수 설정이 추가적으로 가능합니다

> *설문 화면예시* <br>
> <img src="https://user-images.githubusercontent.com/39648594/61213757-ebf5a280-a740-11e9-9bb3-b65fbc91de78.png">

> *객관식 보고서 예시*<br>
> <img src="https://user-images.githubusercontent.com/39648594/61209592-222d2500-a735-11e9-8a7d-23884cf7c04a.png">

> *주관식 보고서 예시*<br>
>  <img src="https://user-images.githubusercontent.com/39648594/61212454-50aefe00-a73d-11e9-99b0-c468ffab5507.png">
  
### 실적평가
- MBO를 이용한 실적평가는 단계를 셋(Plan - Do - See)으로 나누어 프로세스를 진행합니다. 이는 상사와 부하가 목표에 대해 지속적으로 소통하게 만들고, 보다 확실하게 조직원들이 목표를 달성할 수 있도록 돕습니다. 또한, 최종적으로 피평가자의 평가에 대한 납득도를 높입니다.
- Plan 단계 :
  목표를 작성하는 단계 입니다. 개인이 목표를 설정하고 관계 설정된 인원들(1차 고과자, 2차 고과자, 3차 고과자)과 댓글로 해당 목표에 대한 협의를 진행합니다. (작성한 목표는 본인이 상사로부터 평가 받을 항목이 됩니다.)
- Do 단계 :
  이전에 작성한 목표를 달성하기 위해 실천하는 단계 입니다. 댓글을 통해 본인이 수행한 사실을 기록하거나, 상사에게 알릴 수 있습니다. 상사 역시 목표에 대한 중간 진단을 하거나, 피드백 할 수 있습니다.
  여러 사정과 상황(ex> 회사 재정, 정치, 경제)들로 인해 목표 변동이 불가피할 때가 있습니다. 때문에, Do 단계에서의 목표는 수정/삭제가 가능하되, 내역이 기록으로 남게 됩니다.
- See 단계 :
  See 단계는 최종적으로 각 목표에 대한 달성도를 평가하는 단계 입니다. 본인 평가와 상사 평가가 독립적으로 이루어지며, 상사는 피평가자의 평가 내용을 확인하면서 평가를 진행할 수 있습니다. 때문에, 1차적 평가 협의 과정이 자연스럽게 형성 됩니다. 평가가 모두 끝나면 상사 면담, 평가 조율을 거쳐 하나의 프로세스가 완전히 끝이 납니다. 이후, 상사 피드백이 진행되면서 프로세스는 다음 분기 목표 작성 단계(Plan)로 이어지게 됩니다.

> *목표 작성 화면 예시*<br>
> <img src="https://user-images.githubusercontent.com/39648594/61212998-c2d41280-a73e-11e9-9eef-88c619be92f5.png">

>  *목표 평가 화면 예시*<br>
> <img src="https://user-images.githubusercontent.com/39648594/61214685-8bb43000-a743-11e9-95fc-58b60a536676.png">

> + 각 목표는 1.목표, 2.달성기준, 3.비중으로 구성됩니다. (비중은 본인의 100% 목표 구성 중 해당 목표가 몇 %의 비율을 차지하는 지 나타냅니다.)
> + 각 부서의 팀장들은 plan 단계 시 팀 목표를 작성하며, 이는 팀원들이 목표 작성 시 공유되게 됩니다.
> +  각 단계(plan-do-see)마다 평가자가 피평가자에 대한 면담내용을 기록해 둘 수 있습니다.
> +  상사는 평가 시 부하량이란 항목을 따로 평가함으로써 해당 분기가 끝날 때 개인의 업무 과중도를 추가로 평가할 수 있습니다.

## 설정
- 공통 설정
  1. 회사 등록
  2. 평가 회차 등록
  3. 직원 등록
+ 다면평가
  1. 운영 정보 등록 (제목, 내용, 상태, 실시일자 등의 세부사항)
  2. 직원 간 관계 설정
  3. 척도 설정
  4. 개인이 평가 받을 객관식 항목 설정 (직군-계층 별로 설정)
  5. 주관식 질의 설정
+ 실적평가
  1. 운영 정보 등록 (제목, 내용, 상태, 실시일자 등의 세부사항)
  2. 직원 간 관계 설정
  3. 척도 설정(달성도, 부하량)
## 세부 기능
- 부트스트랩을 기반으로 작성했기 때문에 반응형 페이지를 지원합니다.
- 관리자 기능 차등을 지원합니다. ADMIN, MANAGER, BASIC으로 역할 구분이 가능하고 ADMIN 이하의 역할을 가진 관리자는 일부 수정/삭제 기능이 비활성화 됩니다. 때문에, 보다 안정적이고 원활하게 운영 담당자 배정이 가능합니다.
- 모든 설정(직원, 항목, 관계)은 xl파일로 업로드/다운로드 가능합니다. 때문에, 설정 파일을 작성할 고객사와의 소통을 보다 원활하게 할 수 있습니다.
- 모든 평가 데이타는 관리자 페이지에서 실시간으로 집계되며, 다운로드가 가능합니다.
- 모든 진행 상황은 관리자 시스템에서 모니터링 가능합니다. (평가 진행률, 목표 작성률, 팀 목표 작성현황 등)
- 직원의 패스워드는 무작위 생성을 기본으로 합니다.
- 직원의 정보 변경 시 고객사에서 전달받은 파일을 재업로드하면 해당 내용으로 수정됩니다.
- 항목, 관계 설정은 회차마다 설정되기 때문에 초기화가 가능합니다.
- 평가 상태는 실시 전, 임시 저장, 최종 제출로 구분되며 최종 제출 후에는 평가 재실시가 불가능합니다.
- 고객의 요청에 의해 불가피하게 평가 상태를 변경해주어야 하는 경우에는 관리자 페이지에서 변경이 가능합니다.
- Mbo 실적평가 진행 시 등록, 수정된 관계인의 목표, 댓글은 메인 페이지에서 확인이 가능합니다.