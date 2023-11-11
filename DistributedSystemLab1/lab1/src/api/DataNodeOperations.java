package api;


/**
* api/DataNodeOperations.java .
* 由IDL-to-Java 编译器 (可移植), 版本 "3.2"生成
* 从api.idl
* 2023年11月11日 星期六 下午11时30分40秒 CST
*/

public interface DataNodeOperations 
{
  byte[] read (int block_id);
  int append (int block_id, byte[] bytes);
  int randomBlockId ();
} // interface DataNodeOperations
