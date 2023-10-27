# Lab 1 实验报告

包 括 但 不 限 于 FsImage的 设 计 ， 文 件 和 数 据 块 的 映 射 和 数 据 块 定 位 、 多 副 本 的 实 现 、 读 数 据 多个 DateNode 可 用 时 的 选 择 策 略 。

### Part 1. 结构设计

**1. 数据存储**

- 文件数据
  - 文件被切分为块存在DataNode对应的block下
  - 每个DataNode在初始化时load对应DataNode_N文件夹下的数据
- 文件元数据
  - 文件元数据存储在NameNode中
  - NameNode负责维护filepath -> filedesc.id的映射
  - NameNode通过load Fsimage来初始化

**2. 通信**

- 读取文件
  - Client向NameNode发起读请求，NameNode检验是否合法
  - NameNode根据存储的元数据，返回文件所在的block_id以及对应的DataNode
  - Client向DataNode传递block_id并索要数据
- 创建/修改文件
  - Client将文件切块，向NameNode发起请求，NameNode检验是否合法
  - NameNode为写请求分配三个DataNode，并向用户返回DataNode，等待DataNode回复
  - Client向DataNode传递数据并写入
  - 所有DataNode都完成写入后，报告给NameNode已完成，并向NN传递block_id
  - NameNode更新文件元数据