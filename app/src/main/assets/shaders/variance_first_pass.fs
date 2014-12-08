#version 300 es
precision highp float;

layout (location=0) out vec4 vFragColor;

smooth in vec clipSpacePos;

void main()
{
	// -1 to 1
	vec3 position = clipSpacePos.xyz / clipSpacePos.w;
	
	// Add some offset to remove shadow acne
	pos.z += 0.001;
	
	// 0 to 1
	float depth = (pos.z + 1.0) * 0.5;
	
	float moment1 = depth;
	float moment2 = depth * depth;
	
	vFragColor = vec4(moment1, moment2, 0.0, 0.0);
}