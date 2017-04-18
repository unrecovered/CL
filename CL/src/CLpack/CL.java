package CLpack;
import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.JFrame;

import org.bridj.Pointer;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLPlatform.DeviceFeature;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;
import com.nativelibs4java.util.IOUtils;

public class CL {
	
	private static int[] colors;
	private static String title = "Bacs v2.00";
	
	public static short mapHeight = 200;
	public static short mapWidth = 200;
	public static short scale = 2;
	
	CL(){
		//тут будет загрузка из файла
	}

	public static void main(String[] args) throws IOException {
		
		short framerate = 60;
		int second = 1000000000;
		
		String KernelSource = IOUtils.readText(CL.class.getResource("kernel.c"));
		//System.out.print(KernelSource);
		
		CLContext _context = JavaCL.createBestContext(DeviceFeature.GPU);//MaxComputeUnits); //контекст
		CLQueue _queue = _context.createDefaultQueue(); //очередь
		CLProgram program = _context.createProgram(KernelSource); //сборка программы
		CLKernel _actkern = program.createKernel("action"); //выбор ядра
		
		//цвета
		CLBuffer<Integer> colorBuf = _context.createIntBuffer(CLMem.Usage.Input, mapHeight*mapWidth); 
		Pointer<Integer> colorPtr = colorBuf.map(_queue, CLMem.MapFlags.Write);
		colors = new int[mapWidth*mapHeight];
		for(int i=0; i<mapHeight*mapWidth; i++){
			colors[i] = 0;
		}
		colorPtr.setInts(colors);
		colorBuf.unmap(_queue, colorPtr); 
		
		//рандом
		CLBuffer<Integer> randomBuf = _context.createIntBuffer(CLMem.Usage.Output, mapWidth*mapHeight);
		Pointer<Integer> randomPtr = randomBuf.map(_queue, CLMem.MapFlags.Write);
		int[] randoms;
		randoms = new int[mapHeight*mapWidth];
		for(int i=0; i<mapHeight*mapWidth; i++){
			randoms[i] = getRandom(0, 16777215);
		}
		randomPtr.setInts(randoms);
		randomBuf.unmap(_queue, randomPtr);
		
		//аргументы
		_actkern.setArg(0, colorBuf);
		_actkern.setArg(1, randomBuf);
		
		//гуй
		JFrame window=new JFrame(title);
		window.setSize(mapWidth*scale+15, mapHeight*scale+38);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLayout(new BorderLayout(1,1));
		Canvas playfield=new Canvas(colors);
		playfield.setSize(mapWidth*scale, mapHeight*scale);
        window.add(playfield);		
        window.setVisible(true);
		
		long start = System.nanoTime();
		long redraw = start;
		
		for(int i=0; i<100000; i++){
			CLEvent completion = _actkern.enqueueNDRange(_queue, new int[] {mapWidth*mapHeight});
			if(System.nanoTime() - redraw < second/framerate){
				completion.waitFor();
			}else{
				Pointer<Integer> output = colorBuf.read(_queue, completion);
				window.setTitle(title+" Iteration "+i+" out of "+100000+"("+i*100/100000+"% done)");
				playfield.colors = output.getInts(mapWidth*mapHeight);
				playfield.repaint();
				redraw = System.nanoTime();
			}
		}
		
		float passed = (float)(System.nanoTime() - start)/second;
		System.out.println(passed);
	}
	
	public static int getRandom(int min, int max)
	{
		return (int) (Math.floor(Math.random() * (max - min + 1)) + min);
	}

}
