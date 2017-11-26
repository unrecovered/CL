__constant char dirx[] = {0, 1, 1, 1, 0, -1, -1, -1};
__constant char diry[] = {-1, -1, 0, 1, 1, 1, 0, -1};
__constant uchar memsize = 127;
__constant uchar cmdsize = 64;

unsigned int ParallelRNG( unsigned int x )
{
	unsigned int value = x;

	value = (value ^ 61) ^ (value>>16);
	value *= 9;
	value ^= value << 4;
	value *= 0x27d4eb2d;
	value ^= value >> 15;

	return value;
}

unsigned int ParallelRNG2( unsigned int x,  unsigned int y )
{
	unsigned int value = ParallelRNG(x);

	value = ParallelRNG( y ^ value );

	return value;
}

__kernel void reorder(  __global int *statics,
						__global int *order,
						__global int *color,
						__global char *commons )
{	
	size_t id = get_global_id(0);

	int H = statics[0];
	int W = statics[1];
	//for(int i=0; i<1024; i++){
		int x = (order[id]*101 + 1)%(H*W); //вынести коэффициенты
		bool done = false;
		if(color[x] != 0){
			int start = x*memsize;
			while(!done){
				int pointer = commons[start+memsize-1] + start; //номер клетки в абсолютной адресации
				if(commons[pointer] < cmdsize){//переход
					commons[start+memsize-1] = commons[pointer];
					color[x] = ParallelRNG2( color[x], id ) % 16777216;
				}else if(commons[pointer] == memsize-3){//движение
					char dir = commons[start+memsize-20];//проверку на доступность
					int dest = (x%W + dirx[dir]+W)%W+((x/W+diry[dir]+H)%H)*W;
					color[dest] = color[x];
					color[x] = 0;
					commons[start+memsize-1]++;
					for(uchar i=0; i<memsize; i++){
						commons[dest*memsize+i] = commons[start+i];
						commons[start+i] = 0;
					}
					done = true;
				}else if(commons[pointer] == memsize-4){//поворот
					//int dir = commons[start+memsize-20];
					char side = commons[pointer+1]%2; //в какую сторону поворачивать
					if(side == 0){
						commons[start+memsize-20] = (commons[start+memsize-20]+7)%8;
					}else{
						commons[start+memsize-20] = (commons[start+memsize-20]+1)%8;
					}
					commons[start+memsize-1] += 2;
				}
				commons[start+memsize-1] %= cmdsize;
			}
		}
		order[id] = x;
	//}
}