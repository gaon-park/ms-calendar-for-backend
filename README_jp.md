# Game Utility Site (2023.01 ~ 2023.04)

- 確率系アイテム記録検証サイト
- 共有カレンダー（Utility Site Backend Server）
- [アイテム使用結果取得 API](https://developers.nexon.com/Maplestory/api/15/47) 使用
- 確率系アイテムの記録を検索し、公開確率の正確度を検証する
- ユーザ間フォローシステム構築
- 詳細内容 repo
    - backend: https://github.com/gaon-park/ms-calendar-for-backend
    - frontend: https://github.com/gaon-park/ms-calendar-for-frontend

## Index
1. [技術](https://github.com/gaon-park/ms-calendar-for-backend/tree/master#%EA%B8%B0%EC%88%A0-%EC%8A%A4%ED%83%9D)
2. [運営関連](https://github.com/gaon-park/ms-calendar-for-backend/tree/master#%EC%9A%B4%EC%98%81-%EA%B4%80%EB%A0%A8-%EB%A9%94%EC%9D%B4%ED%94%8C-inven-%EC%9D%B4%EC%9A%A9)
3. [機能一覧](https://github.com/gaon-park/ms-calendar-for-backend/tree/master#%EA%B8%B0%EB%8A%A5-%EC%82%AC%ED%95%AD)
    1. [詳細API確認 (swagger)](https://github.com/gaon-park/ms-calendar-for-backend/tree/master#%EC%83%81%EC%84%B8-api-%ED%99%95%EC%9D%B8-swagger)
    2. [ユーザ全般](https://github.com/gaon-park/ms-calendar-for-backend/tree/master#%EC%9C%A0%EC%A0%80-%EA%B4%80%EB%A0%A8)
    3. [日程全般](https://github.com/gaon-park/ms-calendar-for-backend/tree/master#%EC%9D%BC%EC%A0%95-%EA%B4%80%EB%A0%A8)
    4. [アイテム履歴全般](https://github.com/gaon-park/ms-calendar-for-backend/tree/master#%ED%81%90%EB%B8%8C-%EC%9D%B4%EB%A0%A5-%EA%B4%80%EB%A0%A8)

## 技術

| 分類        | 技術                                                                |
|-----------|-------------------------------------------------------------------|
| Language  | - Kotlin<br/>- TypeScript                                         |
| Framework | - SprintBoot<br/>- Next.js                                        |
| DB        | - MySQL                                                           |
| GCP       | - Compute Engine <br/>- IAM <br/>- Cloud Storage <br/>- VPC 네트워크  |
| Server    | - Nginx (Reverse Proxy)                                           |
| Tools     | - Docker                                                          |

## 運営関連 (韓国ゲームコミュニティ)
- [テスト手伝ってください！](https://www.inven.co.kr/board/maple/5974/940125)
- [自分の幸運を確認してみよう! 機能説明版](https://www.inven.co.kr/board/maple/5974/989320)
- [サーバ下げます…😥](https://www.inven.co.kr/board/maple/5974/1400582)

## 機能一覧
### 機能一覧 (swagger)
- json 形式: https://github.com/gaon-park/ms-calendar-for-backend/blob/master/api-docs.json
- yaml 形式: https://github.com/gaon-park/ms-calendar-for-backend/blob/master/api-docs.yml
- ローカルサーバ起動時: http://127.0.0.1/api-docs/
- サービスサーバ: https://ms-hero.kr/api-docs/
    ** 管理者 ID/PW 必要

### ユーザ全般
0. Google OAuthで新規加入／ログインします。
1. アカウントを非公開に設定する場合、フォロワじゃないユーザにはフォロー情報を提供しません。
    - アカウントのいろんな情報を設定できます。

   ![image](https://github.com/gaon-park/ms-calendar-for-backend/assets/52269983/37a7d7df-8553-490b-a104-8509ab578d29)

2. フォローをリクエストしたり、キャンセルすることができます
    ![image](https://github.com/gaon-park/ms-calendar-for-backend/assets/52269983/c5de095b-b812-4b62-b191-2faf5800ec4e)

    ![image](https://github.com/gaon-park/ms-calendar-for-backend/assets/52269983/3a69d396-b76d-4ce4-8468-4a28d7859d44)


3. フォロー状態を管理することができます。
    - リクエストを承認
    - 私のフォロワーから削除
  
    ![image](https://github.com/gaon-park/ms-calendar-for-backend/assets/52269983/22d59b52-09cc-4176-809a-04259b55a673)


4. 検索は1回最大1000件まで可能です。結果値が 1000 件以上の場合は、キーワードやフィルターを使用して検索範囲を減らしてください。

    ![image](https://github.com/gaon-park/ms-calendar-for-backend/assets/52269983/deea0fd4-e487-4ecd-a15f-4128fe299c4b)

5. アイテム使用データを確認するためのAPI KEY登録が必要です。
    ![image](https://github.com/gaon-park/ms-calendar-for-backend/assets/52269983/ec9aedfd-6caf-4a78-8784-3282b1f7206d)

6. アラームを使用して、リアルタイムフォローリクエスト、スケジュール招待などの情報を受け取ることができます。

    ![image](https://github.com/gaon-park/ms-calendar-for-backend/assets/52269983/a4751050-b684-4dc2-882e-c8c479291391)

### 日程全般
0. ゲーム公式イベントと自分の日程、アカウントの状態が公開であったり、フォロー中のユーザの公開日程を確認することができます。

    ![image](https://github.com/gaon-park/ms-calendar-for-backend/assets/52269983/dbb70a1d-beb2-4058-82ab-0a7f1686a530)

1. 日程を生成するとき、いろんな設定ができます。
    - アカウントの公開、非公開状態に関わらず、「参加者だけに」を設定すると日程のメンバーだけが見れたり、修正することができます。
    - アカウントの状態が非公開であれば、「みんなに」日程は日程のメンバー、またわフォロワーだけが見れます。
    - 日程追加、修正の時のメンバー招待はフォロワーであったり、アカウント公開状態のユーザにだけ送れます。
    - 反復日程は終了日未設定の時、最大1年後まで反復します。

    ![image](https://github.com/gaon-park/ms-calendar-for-backend/assets/52269983/45690357-27c7-4136-afa2-487b52b903c9)

### アイテム履歴全般
0. [ゲーム公式サイトの公開確率](https://maplestory.nexon.com/Guide/OtherProbability/cube/red)とユーザの実際のデータを比較／検証します。

   ![image](https://github.com/gaon-park/ms-calendar-for-backend/assets/52269983/93117bbb-c188-4198-b1ec-c39afb7d96b1)

2. 全体登録ユーザと自分のアイテム使用履歴を確認することができます。
    - 最近１ヶ月データ
    - 最近２ヶ月データ
    - 最近３ヶ月データ
    - 全体累積データ
   
    ![image](https://github.com/gaon-park/ms-calendar-for-backend/assets/52269983/974c69d5-ef7f-46aa-8ac2-063f98ceb209)

    ![image](https://github.com/gaon-park/ms-calendar-for-backend/assets/52269983/c74414b0-3c74-4122-8de2-a2be84b1a6b1)

3. 自分の履歴を確認するとき、アイテム別確率差を追加で確認することができます。
    - ex
    - 下のアイテムについては運がすごく悪かったが、
   
    ![image](https://github.com/gaon-park/ms-calendar-for-backend/assets/52269983/1d766f05-3e20-4497-a80f-a84a8a932bc8)

    - 次のアイテムについては、運がすごく良かったことがわかります。

    ![image](https://github.com/gaon-park/ms-calendar-for-backend/assets/52269983/82d064e2-4a72-45c5-825b-2e9b6c8b1ead)

    
5. 自分の詳細履歴を確認することができます。
    - export ボタンで csv 形式出力もできます。

    ![image](https://github.com/gaon-park/ms-calendar-for-backend/assets/52269983/6ffdf851-4729-4ec6-9695-902457836da9)
