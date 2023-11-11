package api;


/**
* api/NameNodeOperations.java .
* 由IDL-to-Java 编译器 (可移植), 版本 "3.2"生成
* 从api.idl
* 2023年11月8日 星期三 下午06时27分30秒 CST
*/

public interface NameNodeOperations 
{
  String open (String filepath, int mode);
  void close (String filepath);
  void modifyBlockID (String filepath, int new_block_id);
} // interface NameNodeOperations
