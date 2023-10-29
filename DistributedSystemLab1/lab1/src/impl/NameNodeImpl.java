package impl;
//TODO: your implementation
import api.NameNodePOA;
import java.util.HashMap;
import utils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public class NameNodeImpl extends NameNodePOA {
    private List<FileDesc> file_descriptor;   //元数据列表
    private HashMap<String, int> path_descriptor;   //file_path -> descriptor

    //初始化，读取fsimage并载入
    public NameNodeImpl(){
        StringBuilder json_content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader("fsimage.json"))) {
            String line;
            while ((line = br.readLine()) != null) {
                json_content.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 将JSON字符串转换为List<FileDesc>对象
        // todo：考虑为空的处理
        String json_string = json_content.toString();
        ObjectMapper object_mapper = new ObjectMapper();
        try {
            this.file_descriptor = object_mapper.readValue(json_string, new TypeReference<List<FileDesc>>() {});
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String open(String filepath, int mode) {
        int descriptor_id = this.path_descriptor.get(filepath);

        if(descriptor_id == null){
            int next_id = file_descriptor.length + 1;
            long time_now = System.currentTimeMillis();
            this.file_descriptor.add(new FileDescr(next_id,mode,0,time_now,time_now,time_now));
            descriptor_id = next_id;
        }
        FileDesc file = file_descriptor[descriptor_id];

        if (file.getMode() & 0b10){
            return null;
        }
        file.setMode(mode);

        //todo：传回需要的信息
        String fd = file.toString();
        return fd;
    }


    @Override
    public void close(String filepath) {
        int descriptor_id = this.path_descriptor.get(filepath);
        if(descriptor_id == null){
            //todo: 错误处理
        }

        FileDesc file = file_descriptor[descriptor_id];

        //关闭了没有打开过的文件
        if (!(file.getMode() & 0b11)){
            return;
        }

        //修改元数据
        long time_now = System.currentTimeMillis();
        file.setAccess_time(time_now);
        if (file.mode & 0b10){
            file.setModified_time(time_now);
        }
        file.setMode(0b00);

        //todo：更新fsimage信息
        try (FileWriter file_writer = new FileWriter("fsimage.json")) {
            ObjectMapper object_mapper = new ObjectMapper();
            object_mapper.writeValue(file_writer, this.file_descriptor);
            System.out.println("文件元数据已成功更新");
        } catch (IOException e) {
            e.printStackTrace();
            // 处理文件写入异常
        }


    }
}
