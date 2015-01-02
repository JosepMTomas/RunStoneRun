#version 300 es

layout (location = 0) in vec4 aPosition;
layout (location = 1) in vec2 aTexCoord;

layout (std140) uniform grassProperties
{
	vec4 properties[1024];
};

layout (std140) uniform lightInfo
{
	vec3 vLight;
	vec4 lightColor;
	vec4 ambientColor;
};

uniform sampler2DShadow shadowMapSampler;
uniform mat4 shadowMatrix;
uniform mat4 viewProjection;
//uniform int lod;

out vec2 vTexCoords;
out float vDistance;
out float vShadows;
out vec4 vAmbient;
out vec4 vLightColor;
//out vec4 vLodColor;

void main()
{
	vec4 current = properties[gl_InstanceID];
	//vec4 position = (aPosition * current.z) + vec4(current.x, 0f, current.y, 0f);
	vec4 position = aPosition;
	position.y *= current.z;
	position += vec4(current.x, 0f, current.y, 0f);
	
	vec4 shadowCoords = shadowMatrix * position;

	vTexCoords = aTexCoord;
	vDistance = current.w;
	
	// Shadow mapping
	float diffuse = dot(vec3(0.0, 1.0, 0.0), vLight);
	float shadow = textureProj(shadowMapSampler, shadowCoords);
	vShadows = diffuse * shadow;
	//vShadows = shadow + 0.2;
	/*vShadows = vec3(shadow);
	vShadows += vec3(0.137, 0.169, 0.441);
	vShadows = clamp(vShadows, 0.0, 1.0);*/
	
	//LOD color
	/*switch(lod)
	{
		case 0: vLodColor = vec4(0.0, 1.0, 0.0, 1.0); break;
		case 1: vLodColor = vec4(1.0, 0.0, 0.0, 1.0); break;
		default: vLodColor = vec4(1.0, 0.0, 1.0, 0.0); break;
	}*/
	
	vAmbient = ambientColor;
	vLightColor = lightColor;
	
	gl_Position = viewProjection * position;
}