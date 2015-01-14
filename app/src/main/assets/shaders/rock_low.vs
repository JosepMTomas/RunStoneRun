#version 300 es

layout (location = 0) in vec4 aPosition;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;

layout (std140) uniform rockProperties
{
	vec4 properties[512];
};

layout (std140) uniform lightInfo
{
	vec3 vLight;
	vec4 lightColor;
	vec4 ambientColor;
	vec4 backColor;
};

uniform mat4 viewProjection;
uniform sampler2D diffuseSampler;

out vec4 vColor;

void main()
{
	vec4 property = properties[gl_InstanceID];
	
	// final position
	vec4 position = aPosition * property.z;
	position.x += property.x;
	position.z += property.y;
	position.w = 1.0;
	
	// final distance
	//float dist = property.w;
	
	// diffuse component
	vec4 diffuseTex = texture(diffuseSampler, aTexCoord);
	float diffuse = dot(aNormal, vLight);
	diffuse = clamp(diffuse, 0.0, 1.0);
	diffuseTex *= (lightColor * diffuse);
	
	vColor = mix(diffuseTex, backColor, property.w);
	
	// vertex shader output
	gl_Position = viewProjection * position;
}