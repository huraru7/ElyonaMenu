# ElyonaMenu

Elyona World の各機能(経済・アイテム・ランクなど)へアクセスするための統合メニュー(MIMIC端末)を提供するプラグイン。

## 主な機能

- MIMIC端末からの統合メニューGUI(残高・アイテム売却・ランク・称号・ステータス表示など)
- 各プラグイン(Economy/Items/Rank)の情報をまとめて表示

## コマンド

| コマンド | 説明 | 権限 |
|---|---|---|
| `/menu` | MIMIC端末(統合メニュー)を開く | `elyona.menu.use`(デフォルト全員) |
| `/mimicterminal` | MIMIC端末アイテムを入手する | `elyona.admin` |

## 依存関係

- Paper 1.21.1
- [ElyonaCore](https://github.com/huraru7/ElyonaCore)
- [ElyonaEconomy](https://github.com/huraru7/ElyonaEconomy)
- [ElyonaItems](https://github.com/huraru7/ElyonaItems)
- [ElyonaRank](https://github.com/huraru7/ElyonaRank)

## ビルド

```
./gradlew build
```

Java 21 / Paper 1.21.1 (paperweight userdev) を使用。
