package api;


/**
* api/NameNodeOperations.java .
* 由IDL-to-Java 编译器 (可移植), 版本 "3.2"生成
* 从api.idl
* 2023年11月12日 星期日 下午12时56分17秒 CST
*/

public interface NameNodeOperations 
{
  String open (String filepath, int mode);
  void close (String filepath);
  void modifyBlockInfo (String filepath, int new_block_id, int size, int time);
} // interface NameNodeOperations
