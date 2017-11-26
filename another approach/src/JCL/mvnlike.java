package JCL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import com.nativelibs4java.opencl.*;
import com.nativelibs4java.opencl.CLMem.Usage;
import com.nativelibs4java.opencl.CLPlatform.ContextProperties;
import com.nativelibs4java.opencl.CLPlatform.DeviceFeature;
import com.nativelibs4java.util.*;

import org.bridj.Pointer;
import static org.bridj.Pointer.*;

import java.awt.BorderLayout;
import java.io.IOException;
import java.nio.ByteOrder;

import javax.swing.JFrame;

public class mvnlike {
	
	public static int H = 16;
	public static int W = 32;
	
	static int scale = 10; //введу потом
	
    public static void main(String[] args) throws IOException, InterruptedException {
    	
        List<CLDevice> devices = new ArrayList<>(); //прикрутить приличный интерфейс
        for (CLPlatform platform : JavaCL.listPlatforms()) {
        	
            try { //разделить обработку для листа устройств и недоступного устройства
				for (CLDevice device : platform.listAllDevices(true)) {
				    JavaCL.createContext(Collections.<ContextProperties, Object> emptyMap(), device);
				    System.out.println(device.getName());
				    devices.add(device);
				}
			} catch (Exception e) { //сделать вывод ошибок в терминал 
				e.printStackTrace();
			} continue;
        }//обработка ошибки когда ни одно доступное устройство не найдено
    	
        byte memsize = 127;
        byte cmdsize = 64;
        
		//context = JavaCL.createBestContext(DeviceFeature.GPU);
		CLContext context = JavaCL.createContext(Collections.<ContextProperties, Object> emptyMap(), devices.get(0));
        CLQueue queue = context.createDefaultQueue();
        ByteOrder byteOrder = context.getByteOrder();
        
        Pointer<Integer>
            statPtr = allocateInts(2).order(byteOrder),
            valuePtr = allocateInts(H*W).order(byteOrder),
        	colorPtr = allocateInts(H*W).order(byteOrder);
        
        Pointer<Byte> 
        	bacsPtr = allocateBytes(H*W*memsize).order(byteOrder); //memsize в переменную
        
        statPtr.set(1, W);
        statPtr.set(0, H);
        
        for(int i=0; i<H*W; i++){
        	valuePtr.set(i, i);
        }
        
        byte firstel[] = new byte[memsize];
        firstel[0] = (byte)(memsize-3);
        firstel[1] = (byte)(memsize-4);
        firstel[2] = 3;
        firstel[memsize-20] = 0; //направление
        
        bacsPtr.setArrayAtOffset((H*W/2+W/2)*memsize, firstel);
        
        colorPtr.setIntAtIndex(H*W/2+W/2, 16711680);

        // Create OpenCL input buffers (using the native memory pointers aPtr and bPtr) :
        CLBuffer<Integer> 
            a = context.createBuffer(Usage.Input, statPtr),
            b = context.createBuffer(Usage.Input, valuePtr),
        	c = context.createBuffer(Usage.InputOutput, colorPtr);
        
        CLBuffer<Byte>
        	d = context.createBuffer(Usage.Input, bacsPtr);

        // Create an OpenCL output buffer :
        //CLBuffer<Integer> out = context.createIntBuffer(Usage.Output, H);

        // Read the program sources and compile them :
        String src = IOUtils.readText(mvnlike.class.getResource("kernel.c"));
        CLProgram program = context.createProgram(src);

        // Get and call the kernel :
        CLKernel reorderKernel = program.createKernel("reorder");
        reorderKernel.setArgs(a, b, c, d);
        
      //гуй
        Random rnGen = new Random();
        
      	JFrame window=new JFrame("v2.0");
      	window.setSize(W*scale+15, H*scale+38);
      	window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      	window.setLayout(new BorderLayout(1,1));
      	Canvas playfield=new Canvas(new int[H*W]);
      	playfield.setSize(W*scale, H*scale);
        window.add(playfield);		
        
        //дефолтное заполнение, для дебага
        for(int i=0; i<H*W; i++){
        	playfield.colors[i] = rnGen.nextInt(16777215);
        }
        
        window.setVisible(true);
        
        long start = System.nanoTime();
        
        for(int i=0; i<100; i++){
        	Thread.sleep(1000);
        	CLEvent addEvt = reorderKernel.enqueueNDRange(queue, new int[] { H*W });
        	Pointer<Integer> outPtr = c.read(queue, addEvt); 
        	playfield.colors = outPtr.getInts(H*W);
        	playfield.repaint();
        	System.out.println("Cycle: "+i);
        }
        
        long passed = (System.nanoTime() - start)/1000000000;
        System.out.println("Done in "+passed/60+"m "+passed%60+"s");
        // Print the first 10 output values :
        //for (int i = 0; i < 10; i++)
        //    System.out.println("out[" + i + "] = " + outPtr.get(i));
        
    }
}
