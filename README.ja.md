# 母校のスクールバス予約システム（運転手用アプリ・リファクタリング版）

- RefacSchoolBusReservationForDriver-kotlin  
- 2024 大邱カトリック大学 キャップストーンデザイン 🥉奨励賞受賞作品（スクールバス予約システム）の Kotlin リファクタリング版です。

- スクールバス運行のための **運転手専用アプリ**です。  
- Firebase を活用し、**リアルタイム予約者数の確認**、**運行記録の確認**、  
- **簡単な時間／路線の管理**ができるように設計されています。  
- **デモ動画を見る** [📽️](https://youtube.com/shorts/iBq0nHXdfOc?feature=share)
- 📄 [🇰🇷 韓国語版READMEはこちら](./README.md)

---

## 主な機能（画面別に紹介）

### 1. ログイン [Login.kt](app/src/main/java/com/example/refac_driverapp/Login.kt) / 会員登録画面 [Register.kt](app/src/main/java/com/example/refac_driverapp/Register.kt)
- [ログイン／会員登録](https://github.com/wonna-0830/login)
<a href="https://github.com/wonna-0830/login">
  <img src="images/login.jpg" width="180">
  <img src="images/register.jpg" width="180">
</a>

- [ログイン XML](app/src/main/res/layout/activity_login.xml)  
- [会員登録 XML](app/src/main/res/layout/activity_register.xml)  
- Firebase Authentication を用いたメールアドレスとパスワードによるログイン  
- 自動ログイン機能（チェックボックス）  
- 運転手専用アカウントとユーザーアカウントを区別してログイン処理  
- フォーマット不正や未入力の場合はログインボタン無効化  
- パスワードが8文字未満、または未入力の際は登録無効化

---

### 2. 路線および時間選択画面 [RouteTime.kt](app/src/main/java/com/example/refac_driverapp/RouteTime.kt)
- [レイアウト](https://github.com/wonna-0830/routetime)
<a href="https://github.com/wonna-0830/routetime">
  <img src="images/routetime.jpg" width="180">
</a>

- [XML](app/src/main/res/layout/activity_route_time.xml)  
- Spinner にて運転可能な路線選択  
- 選択された路線に応じて時間リストが自動更新  
- 両方の選択が完了した時のみ確認ボタンが有効に  
- 確認ボタンを押すと Clock ページに遷移＋選択情報がDBに保存  
- 現在時刻以降の時間のみ Spinner に反映

---

### 3. 運行画面 [Clock.kt](app/src/main/java/com/example/refac_driverapp/Clock.kt)
- [レイアウト](https://github.com/wonna-0830/clock)
<a href="https://github.com/wonna-0830/clock">
  <img src="images/clock.jpg" width="180">
</a>

- [XML](app/src/main/res/layout/activity_clock.xml)  
- 選択した路線と時間に基づき予約された停留所のリストを表示  
- 各停留所ごとの予約人数をリアルタイムで表示（RecyclerView 使用）  
- 予約人数に応じてバッジの色が変化（例：0人=グレー、多い=強調色）  
- 高齢の運転手を考慮し、大きくて太字のテキストを採用  
- ローディング中は ProgressBar を表示  
- 画面上部にデジタル時計を表示  
- 運転中の誤操作防止機能あり  
  - Android デバイスの戻るボタン無効化（Toast で案内）  
  - 運行終了ボタンを2回押さなければページ遷移不可  
- 運転中は画面スリープを防止するロジックを適用

---

### 4. 運行終了画面 [Finish.kt](app/src/main/java/com/example/refac_driverapp/Finish.kt)
- [レイアウト](https://github.com/wonna-0830/finish)
<a href="https://github.com/wonna-0830/finish">
  <img src="images/finish.jpg" width="180">
</a>

- [XML](app/src/main/res/layout/activity_finish.xml)  
- 該当運転手が運行した路線・時間・終了時刻を表示  
- RouteTime.kt にて保存された情報に `endTime` を追加で保存  
- 次の路線選択、運行履歴の確認、ログアウト、アプリ終了 の4ボタン  
- 各ボタンにはアラートメッセージを適用して誤操作を防止

---

### 5. 運行履歴画面 [SelectBusList.kt](app/src/main/java/com/example/refac_driverapp/SelectBusList.kt)
- [レイアウト](https://github.com/wonna-0830/selectbuslist)
<a href="https://github.com/wonna-0830/selectbuslist">
  <img src="images/selectbuslist.jpg" width="180">
</a>

- [XML](app/src/main/res/layout/activity_selectbuslist.xml)  
- 過去の運行履歴を運転手ごとに確認（降順表示）  
- RecyclerView で記録一覧を表示  
- 戻るボタンを押すと Finish 画面に戻る  
- 各予約項目に削除ボタンあり  
  - 路線や時間を誤って選択した場合に限り削除可能  
    - 「選択ミスした路線のみ削除できます。削除されたデータに関する責任は負いかねます。本当に削除しますか？」と確認ダイアログ表示  
- 同一の運行記録がある場合は、**上書き保存により重複を防止**

---

### 6. 共通機能
- Firebase によるリアルタイムデータ連携  
- 戻るボタンを2回押すとアプリ終了

---

## 技術スタック

| カテゴリ | 技術 |
|----------|------|
| 言語 | Kotlin |
| IDE | Android Studio |
| データベース | Firebase Firestore |
| 認証 | Firebase Authentication |
| アーキテクチャ | MVVM 一部適用（今後フル導入予定） |
| その他 | RecyclerView、Spinner、Intent、LiveDataなど |

---

## 🔄 リファクタリングのポイント

- **Java → Kotlinへの完全移行** によってコードの可読性と保守性を向上  
- **UX改善**
  - 運転手の年齢層を考慮し、**大きくて太字のテキスト**を使用  
  - **ライトモード固定**：夜間運転の視認性を考慮し、ダークモード非対応  
  - 一目で把握できる**シンプルで直感的なUI設計**
- **UI強化**
  - 停留所ごとの予約人数に応じて**バッジの色が変化**  
  - **運行履歴ページを追加**し、過去の記録が確認可能に
- **役割分担の明確化**
  - **運転手／ユーザーアカウントの分離**によりセキュリティと機能明確化  
  - **路線・時間選択 → 状況確認** までをシンプルなフローで実現

---

> ✨ このアプリはユーザーアプリとリアルタイムで連携し、  
> 効率的なスクールバス運行をサポートするツールとして設計されています。

