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
	
	unsigned int ParallelRNG3( unsigned int x,  unsigned int y,  unsigned int z )
	{
		unsigned int value = ParallelRNG(x);

		value = ParallelRNG( y ^ value );

		value = ParallelRNG( z ^ value );

		return value;
	}

	__kernel void action(	__global uint *color, __global uint *randomseed	)
	{
		size_t id = get_global_id(0);
		uint randcolor = ParallelRNG2( randomseed[id], id  );
		randomseed[id] = randcolor;
		color[id] = randcolor % 16777216;//idx*idy;
	}