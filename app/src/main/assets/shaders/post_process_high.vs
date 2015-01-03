#version 300 es

layout (location = 0) in vec4 aPosition;
layout (location = 1) in vec2 aTexCoord;

out vec2 vTexCoord;
vec2 pow2(vec2 value, float exponent)
{
	return vec2(pow(value.x, exponent), pow(value.y, exponent));
}

void main()
{
	vTexCoord = aTexCoord;
	
	//vScreenCoord.x = pow(vScreenCoord.x, 2.0);
	//vScreenCoord.y = pow(vScreenCoord.y, 2.0);
	
	gl_Position = aPosition;
}