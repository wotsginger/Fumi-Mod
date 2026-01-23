# 🌸 Fumi Fabric/Neoforge

> **致力于在不同消息平台间传递消息的信使 o(*￣▽￣*)ブ**

Fumi 是一个基于 [NATS 服务器](https://github.com/nats-io) 的消息转发工具，旨在为不同消息平台之间构建一条转发通道。

我们假定消息分布在多个平台，并且存在多个来源，通过内置的 API、协议库、机器人进行相互的消息转发。

所有消息会按照统一的格式发送到 NATS 中心服务器上，再分别由各个客户端进行解析。

目前实现的平台和即将进行适配的平台：

| 平台                  | 支持状况 | 项目地址 |
|---------------------| --- | --- |
| Bikkit/Spigot/Paper | 已支持 | [地址](https://github.com/wotsginger/Fumi-Spigot) |
| QQ（Standalone）      | 已支持 | [地址](https://github.com/wotsginger/Fumi-Standalone) |
| Fabric/Forge             | 已支持 | 你在这里 |
| QQ（Nonebot）         | 已支持 | 暂未发布 |
| KOOK                | 计划中 | 暂未发布 |
| Oopz                | 计划中 | 暂未发布 |
| Discord             | 计划中 | 暂未发布 |

实际上来说，只需要支持将消息以 `{"source":"","message":"","username":""}` 格式发送到 NATS，并支持解析从 NATS 接收到的信息并发送到聊天中即可适配对应平台。如果您有能力，完全可以开发对应的第三方客户端。

---

## 🚀 快速开始

### 1. 环境准备

1. 从我们的 [发布页面](https://www.sitmc.club/download) 下载构建好的 `FumiFabric-xxx.jar` 或者 `FumiNeoForge-xxx.jar`。
2. 将 Jar 文件放入服务器的 `mods` 文件夹中。

### 2. 配置文件

启动服务器后会生成默认配置文件。配置文件位于 `/config/fumi-fabric.json` 或者 `/config/fumi-neoforge.json`。

在配置中填入对应的 NATS 服务器地址和 Token。可以使用我社提供的 `nats://web.sitmc.club:4222`。如果对于信息安全有所顾虑，可以自行部署 NATS 服务器。

* **url / token**: NATS 连接和凭据。
* **subject**: 设置转发对应的频道。Subject 相同的频道内消息会相互转发。支持子节点形式，例如 `sitmc.chat`。
* **sourceName**: 用于标注自身的身份（即消息来源），例如 `survival`。
* **chatFormat**: 消息发送到游戏内的格式。支持变量 `{source}`、`{username}`、`{message}`，支持 `&` 颜色代码。

```json
{
  "url": "nats://web.sitmc.club:4222",
  "token": "",
  "subject": "sitmc",
  "sourceName": "survival",
  "chatFormat": "&2&l{source} &8| &7{username} &8► &7{message}"
}
```
