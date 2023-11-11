package impl;
//TODO: your implementation
import api.NameNodePOA;
import utils.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.*;

public class NameNodeImpl extends NameNodePOA {
    private List<FileDesc> file_descriptor;   //元数据列表
    private HashMap<String, Integer> path_descriptor;   //file_path -> descriptor

    //初始化，读取fsimage并载入
    public NameNodeImpl(){
        String filepath = "../data/fsimage.txt";
        try (FileReader reader = new FileReader(filepath);
             BufferedReader br = new BufferedReader(reader)
        ) {
            String line;
            //网友推荐更加简洁的写法
            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                String line_str =line.replace("\n", "");
                FileDesc my_file = FileDesc.fromString(line_str);
                this.file_descriptor.add(my_file);
                this.path_descriptor.put(my_file.getFilepath(), my_file.getId());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

       /* ObjectMapper objectMapper = new ObjectMapper();
        path_descriptor = new HashMap<>();

        try {
            List<FileDesc> my_files = objectMapper.readValue(new File(filepath), new TypeReference<List<FileDesc>>() {});
            for (FileDesc file : my_files) {
                path_descriptor.put(file.getFilepath(),file.getId());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("load fsimage error");
        }*/

    }

    public void modifyBlockID(String filepath, int new_block_id){
        int descriptor_id = this.path_descriptor.get(filepath);
        FileDesc file = file_descriptor.get(descriptor_id - 1);
        file.addBlockID(new_block_id);
    }

    @Override
    public String open(String filepath, int mode) {
        int descriptor_id = -1;
        if (this.path_descriptor.containsKey(filepath)){
            descriptor_id = this.path_descriptor.get(filepath);
        }
        else{
            int next_id = this.file_descriptor.size() + 1;
            long time_now = System.currentTimeMillis();
            //todo: 怎么roll data node
            int data_node = 0;
            List<Integer> block_id = new ArrayList<>();
            block_id.add(-1);
            this.file_descriptor.add(new FileDesc(next_id,filepath,mode,0,time_now,
                    time_now,time_now,data_node,block_id));
            descriptor_id = next_id;
        }

        FileDesc file = file_descriptor.get(descriptor_id - 1);

        //检查文件是否以w模式open过
        if ((file.getMode() & 0b10) != 0 ){
            return null;
        }
        file.setMode(mode);

        //todo：传回需要的信息
        String meta_data = file.toString();
        return meta_data;
    }


    @Override
    public void close(String filepath) {
        int descriptor_id = this.path_descriptor.get(filepath);
        FileDesc file = file_descriptor.get(descriptor_id - 1);

        //修改元数据：访问时间/修改时间/模式
        long time_now = System.currentTimeMillis();
        file.setAccess_time(time_now);
        if ((file.getMode() & 0b10) != 0){
            file.setModified_time(time_now);
        }
        //这里要改，先不动
        file.setMode(0b00);

        //更新fsimage
        String fs_file_path = "../data/fsimage.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fs_file_path))) {
            for (FileDesc my_file : this.file_descriptor) {
                String data = my_file.toString();
                writer.write(data);
                writer.newLine();  // 添加换行符
            }
            System.out.println("data in fsimage");
        } catch (IOException e) {
            System.err.println("error in fsimage update");
        }

        /*ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(new File("fsimage.json"), this.file_descriptor);
            System.out.println("save fsimage");
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}
