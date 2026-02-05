-- テスト用データ（最終的には取り除く前提）
INSERT INTO category (name, color) VALUES
('仕事', '#1976d2'),
('学習', '#2e7d32'),
('生活', '#6a1b9a'),
('趣味', '#ef6c00');

INSERT INTO todo (author, title, detail, created_at, completed) VALUES
('田中', '買い物リスト作成', '牛乳・卵・パンを購入する', CURRENT_TIMESTAMP, FALSE),
('佐藤', 'レポート提出', '金曜までに資料をまとめる', CURRENT_TIMESTAMP, TRUE),
('鈴木', '運動', '30分のジョギング', CURRENT_TIMESTAMP, FALSE),
('高橋', '部屋の片付け', '本棚と机を整理する', CURRENT_TIMESTAMP, TRUE),
('伊藤', 'メール返信', '取引先への連絡', CURRENT_TIMESTAMP, FALSE),
('渡辺', '旅行計画', '週末の行き先を検討する', CURRENT_TIMESTAMP, FALSE),
('山本', '読書', '技術書を20ページ読む', CURRENT_TIMESTAMP, TRUE),
('中村', '家計簿入力', '今月の支出を整理する', CURRENT_TIMESTAMP, FALSE),
('小林', '会議準備', '議題と資料を確認する', CURRENT_TIMESTAMP, TRUE),
('加藤', '掃除', '玄関とキッチンを掃除する', CURRENT_TIMESTAMP, FALSE),
('吉田', '映画鑑賞', '気になっていた作品を観る', CURRENT_TIMESTAMP, TRUE),
('山田', '学習計画', '今週の学習目標を決める', CURRENT_TIMESTAMP, FALSE);

-- テスト用データ（追加分・最終的には取り除く前提）
INSERT INTO todo (author, title, detail, created_at, completed) VALUES
('斎藤', '朝会メモ作成', '会議で共有するポイントを整理', CURRENT_TIMESTAMP, FALSE),
('石井', '資料レビュー', '設計書の誤字を確認', CURRENT_TIMESTAMP, TRUE),
('前田', 'タスク整理', '今週のToDoを洗い出す', CURRENT_TIMESTAMP, FALSE),
('岡田', 'コードリーディング', '既存実装の流れを把握', CURRENT_TIMESTAMP, TRUE),
('藤田', '議事録作成', '会議内容をまとめる', CURRENT_TIMESTAMP, FALSE),
('小川', '環境確認', 'JavaとMavenのバージョンを確認', CURRENT_TIMESTAMP, TRUE),
('村上', 'UI改善案作成', '一覧画面の改善ポイント整理', CURRENT_TIMESTAMP, FALSE),
('石田', 'バグ再現', '報告された手順で再現確認', CURRENT_TIMESTAMP, TRUE),
('木村', 'テストケース作成', '検索/ソート/ページングのケース作成', CURRENT_TIMESTAMP, FALSE),
('橋本', 'ログ確認', '起動ログの警告を確認', CURRENT_TIMESTAMP, TRUE),
('阿部', 'README整理', '起動手順を追記', CURRENT_TIMESTAMP, FALSE),
('池田', 'DB確認', 'H2コンソールでデータ確認', CURRENT_TIMESTAMP, TRUE),
('森', '画面キャプチャ', '仕様書向けに画面を取得', CURRENT_TIMESTAMP, FALSE),
('山口', 'リリース準備', 'タグ名を決める', CURRENT_TIMESTAMP, TRUE),
('石川', '画面文言確認', 'メッセージの表記ゆれ確認', CURRENT_TIMESTAMP, FALSE),
('松本', '入力チェック確認', '必須項目エラー確認', CURRENT_TIMESTAMP, TRUE),
('井上', '検索精度確認', '部分一致で想定通りか確認', CURRENT_TIMESTAMP, FALSE),
('清水', 'ソート確認', '作成日/タイトル/ステータス順を確認', CURRENT_TIMESTAMP, TRUE),
('森田', 'ページング確認', '前へ/次へ/件数変更を確認', CURRENT_TIMESTAMP, FALSE),
('高木', '完了切替確認', 'トグルで表示が変わるか確認', CURRENT_TIMESTAMP, TRUE),
('中島', '削除確認', 'モーダルの挙動確認', CURRENT_TIMESTAMP, FALSE),
('田口', '入力保持確認', '確認画面から戻った際の保持確認', CURRENT_TIMESTAMP, TRUE),
('安田', 'タイトル重複確認', '同一タイトルで登録できるか確認', CURRENT_TIMESTAMP, FALSE),
('小松', '日付表示確認', '一覧の作成日フォーマット確認', CURRENT_TIMESTAMP, TRUE),
('西村', 'テーブル幅調整', '列幅のバランス確認', CURRENT_TIMESTAMP, FALSE),
('野口', 'フォーム操作確認', 'タブ移動と入力のしやすさ確認', CURRENT_TIMESTAMP, TRUE),
('吉川', '登録フロー確認', '登録→確認→完了の流れ確認', CURRENT_TIMESTAMP, FALSE),
('坂本', '編集フロー確認', '編集→確認→完了の流れ確認', CURRENT_TIMESTAMP, TRUE),
('宮本', '検索クリア確認', 'クリアリンクで全件に戻る確認', CURRENT_TIMESTAMP, FALSE),
('山崎', '完了行表示確認', '完了行のグレー表示確認', CURRENT_TIMESTAMP, TRUE),
('新井', 'アクセシビリティ確認', 'ESCでモーダルが閉じるか確認', CURRENT_TIMESTAMP, FALSE),
('藤井', 'ステータスバッジ確認', '完了/未完了の表示確認', CURRENT_TIMESTAMP, TRUE),
('福田', 'URLパラメータ確認', 'sort/page/sizeが保持されるか確認', CURRENT_TIMESTAMP, FALSE),
('大塚', 'パフォーマンス確認', '50件表示での体感速度確認', CURRENT_TIMESTAMP, TRUE),
('松井', '入力制限確認', '詳細500文字以内の確認', CURRENT_TIMESTAMP, FALSE),
('石原', '検索未入力確認', '未入力で全件表示になるか確認', CURRENT_TIMESTAMP, TRUE),
('西田', 'メッセージ確認', '完了画面の文言確認', CURRENT_TIMESTAMP, FALSE),
('片山', '作業記録整理', '変更内容をまとめる', CURRENT_TIMESTAMP, TRUE),
('本田', '画面遷移確認', '戻るリンクの遷移確認', CURRENT_TIMESTAMP, FALSE),
('長谷川', '検証結果整理', '確認結果のメモ作成', CURRENT_TIMESTAMP, TRUE),
('菊地', 'データ投入確認', 'data.sqlの反映確認', CURRENT_TIMESTAMP, FALSE),
('大野', 'H2設定確認', 'コンソール接続確認', CURRENT_TIMESTAMP, TRUE),
('金子', 'SQL確認', '検索SQLが意図通りか確認', CURRENT_TIMESTAMP, FALSE),
('平田', 'フォーム整形確認', '入力欄の見た目確認', CURRENT_TIMESTAMP, TRUE),
('柴田', 'タイトル検索確認', '部分一致の挙動確認', CURRENT_TIMESTAMP, FALSE),
('永井', 'ソート初期値確認', '初期は作成日降順か確認', CURRENT_TIMESTAMP, TRUE),
('中川', '新規登録確認', '新規登録が成功するか確認', CURRENT_TIMESTAMP, FALSE),
('三浦', '削除取り消し確認', 'キャンセルで削除されないか確認', CURRENT_TIMESTAMP, TRUE),
('木下', '一覧戻り確認', '完了から一覧に戻る確認', CURRENT_TIMESTAMP, FALSE);

-- 既存データのカテゴリ割当（テスト用・最終的には取り除く前提）
UPDATE todo
SET category_id = CASE MOD(id, 4)
    WHEN 0 THEN 4
    WHEN 1 THEN 1
    WHEN 2 THEN 2
    ELSE 3
END;

-- 作成日と期限日をランダムに設定（テスト用・最終的には取り除く前提）
UPDATE todo
SET created_at = DATEADD('DAY', - (MOD(id, 20)), DATEADD('HOUR', MOD(id, 24), CURRENT_TIMESTAMP)),
    deadline = CASE MOD(id, 3)
        WHEN 0 THEN DATEADD('DAY', - (MOD(id, 5) + 1), CURRENT_DATE)  -- 期限切れ
        WHEN 1 THEN DATEADD('DAY', MOD(id, 3), CURRENT_DATE)          -- 期限近い（3日以内）
        ELSE DATEADD('DAY', (MOD(id, 10) + 5), CURRENT_DATE)          -- 期限余裕
    END;
