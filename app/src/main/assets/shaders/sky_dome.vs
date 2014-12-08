#version 300 es

layout (location = 0) in vec4 aPosition;
layout (location = 1) in vec2 aTexCoord;

uniform mat4 modelViewProjection;

out vec4 vPosition;
out vec2 vTexCoord;
out vec3 vColor;

const vec3 upperColor = vec3(0.137, 0.169, 0.441);
//const vec3 upperColor = vec3(0.314, 0.478, 0.773);
const vec3 middleColor = vec3(0.514, 0.737, 0.949);
const vec3 lowerColor = vec3(1.0);

vec3 pow3(vec3 source, float exponent)
{
	return vec3(pow(source.x, exponent), pow(source.y, exponent),pow(source.z, exponent));
}

void main()
{
	vPosition = aPosition;
	vTexCoord = aTexCoord;
	
	float alpha = pow(aTexCoord.y, 2.0);
	float alpha2 = sin(aTexCoord.y * 3.14159);
	
	vColor = mix(lowerColor, middleColor, alpha2);
	vColor = mix(vColor, upperColor, alpha);
	vColor = pow3(vColor, 1.75);
	
	gl_Position = modelViewProjection * aPosition;
}
