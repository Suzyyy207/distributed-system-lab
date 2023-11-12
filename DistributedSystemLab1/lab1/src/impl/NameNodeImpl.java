package impl;
//TODO: your implementation
import api.NameNodePOA;
import utils.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.io.*;

public class NameNodeImpl extends NameNodePOA {
    private List<FileDesc> file_descriptor;   //元数据列表
    private HashMap<String, Integer> path_descriptor;   //file_path -> descriptor
    private int data_node_max = 2;

    //初始化，读取fsimage并载入
    public NameNodeImpl(){
        this.file_descriptor = new ArrayList<>();
        this.path_descriptor = new HashMap<>();
        String filepath = "../src/data/fsimage.txt";

        try (FileReader reader = new FileReader(filepath);
             BufferedReader br = new BufferedReader(reader)
        ) {
            String line;
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


    }

    public void modifyBlockInfo(String filepath, int new_block_id, int new_size, int new_time){
        int descriptor_id = this.path_descriptor.get(filepath);
        FileDesc file = file_descriptor.get(descriptor_id - 1);
        if (new_block_id != -1){
            file.addBlockID(new_block_id);
        }
        file.setSize(new_size);
        file.setModified_time(new_time);
        update();
    }



    @Override
    public String open(String filepath, int mode) {
        int descriptor_id = -1;

        if (path_descriptor.containsKey(filepath)){
            descriptor_id = this.path_descriptor.get(filepath);
        }
        else {
            int next_id = this.file_descriptor.size() + 1;
            int time_now = (int)(System.currentTimeMillis()/1000);
            Random random = new Random();
            int data_node = random.nextInt(data_node_max);
            List<Integer> block_id = new ArrayList<>();
            block_id.add(-1);
            FileDesc new_file = new FileDesc(next_id,filepath,0b00,0,time_now,
                    time_now,time_now,data_node,block_id);
            this.file_descriptor.add(new_file);
            this.path_descriptor.put(filepath, next_id);
            descriptor_id = next_id;
        }

        FileDesc file = file_descriptor.get(descriptor_id - 1);

        //检查文件是否以w模式open过
        if ((file.getMode() & 0b10) != 0 ){
            return "";
        }

        file.setMode(mode);
        update();

        String meta_data = file.toString();
        return meta_data;
    }


    @Override
    public void close(String filepath) {
        int descriptor_id = this.path_descriptor.get(filepath);
        FileDesc file = file_descriptor.get(descriptor_id - 1);

        //修改元数据：访问时间/修改时间/模式
        int time_now = (int)(System.currentTimeMillis()/1000);
        file.setAccess_time(time_now);
        //这里要改，先不动
        file.setMode(0b00);

        update();

    }

    private void update(){
        String fs_file_path = "../src/data/fsimage.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fs_file_path))) {
            for (FileDesc my_file : this.file_descriptor) {
                String data = my_file.toString();
                writer.write(data);
                writer.newLine();  // 添加换行符
            }
        } catch (IOException e) {
            System.err.println("error in fsimage update");
        }

    }



}
