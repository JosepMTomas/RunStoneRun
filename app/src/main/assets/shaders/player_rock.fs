#version 300 es
precision highp float;

layout (std140) uniform lightInfo
{
	vec3 vLight;
	vec4 lightColor;
	vec4 ambientColor;
};

// textures
uniform sampler2D diffuseSampler;
uniform sampler2D normalSampler;

in vec4 vPosition;
in vec2 vTexCoord;
in vec3 vNormal;
in vec3 vTangent;
in vec3 vBinormal;

//in float vDiffuse;
in float vShadows;

out vec4 fragColor;

void main()
{
	lowp vec4 diffuseTex = texture(diffuseSampler, vTexCoord);
	
	lowp vec4 normalTex = texture(normalSampler, vTexCoord);
	normalTex = normalize(normalTex * 2.0 - 1.0);
	mediump vec3 normal = vTangent.xyz * normalTex.x + vBinormal * normalTex.y + vNormal * normalTex.z;
	
	vec4 ambient = diffuseTex * ambientColor;
	
	float diffuse = dot(normal, vLight);
	diffuse = clamp(diffuse, 0.0, 1.0);
	
	float shading = diffuse * vShadows;
	//shading += 0.25;
	shading = clamp(shading, 0.0, 1.0);
	
	fragColor = diffuseTex * shading;
	fragColor += ambient;
}