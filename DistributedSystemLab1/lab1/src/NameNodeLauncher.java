import com.sun.corba.se.spi.orb.ORB;
import java.util.Properties;
import impl.*;
import api.*;
import java.io.IOException;
public class NameNodeLauncher {
    public static void main(String[] args) {
        try {
            Properties properties = new Properties();
            properties.put("org.omg.CORBA.ORBInitialHost", "127.0.0.1");//ORB IP
            properties.put("org.omg.CORBA.ORBInitialPort", "1050"); //ORB port

            // init ORB object
            ORB orb = ORB.init(args, properties);

            // get RootPOA activate POAManager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            // new a object
            NameNodeImpl nameNodeServant = new NameNodeImpl();

            // export
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(nameNodeServant);
            NameNode href = NameNodeHelper.narrow(ref);

            // Naming context
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingcontextExtHelper.narrow(objRef);

            // bind to Naming
            NameComponent[] path = ncRef.to_name("NameNode");
            ncRef.rebind(path, href);
            System.out.println("NameNode is ready and waiting...");

            // waiting
            orb.run();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}