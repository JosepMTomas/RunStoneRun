#version 300 es
precision highp float;

layout (std140) uniform lightInfo
{
	vec3 lightVector;
	vec4 lightColor;
	vec4 ambientColor;
	float shadowFactor;
};

// textures
uniform sampler2D diffuseSampler;
uniform sampler2D normalSampler;

in vec4 vPosition;
in vec2 vTexCoord;
in vec3 vNormal;
in vec3 vTangent;
in vec3 vBinormal;

in float vShadows;

out vec4 fragColor;

const vec3 vLight = normalize(vec3(0.0,0.5,1.0));

void main()
{
	lowp vec4 diffuseTex = texture(diffuseSampler, vTexCoord);
	
	lowp vec4 normalTex = texture(normalSampler, vTexCoord);
	normalTex = normalize(normalTex * 2.0 - 1.0);
	mediump vec3 normal = vTangent.xyz * normalTex.x + vBinormal * normalTex.y + vNormal * normalTex.z;
	
	vec4 ambient = diffuseTex * ambientColor;
	
	float diffuse = dot(normal, lightVector);
	diffuse = clamp(diffuse, 0.0, 1.0);
	
	float diffuse2 = dot(normal, vLight) * (1.0 - shadowFactor);
	diffuse2 = clamp(diffuse2, 0.0, 1.0);
	
	float shading = (diffuse * vShadows) + (1.0 - shadowFactor);
	//shading += 0.25;
	shading = clamp(shading, 0.0, 1.0);
	
	fragColor = (diffuseTex * shading * lightColor) + (diffuseTex * diffuse2);
	fragColor += ambient;
	
	//fragColor = vec4(shadowFactor);
}