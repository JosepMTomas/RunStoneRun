#version 300 es

in vec2 vTexCoord;
in vec3 vNormal;
in vec3 vTangent;
in vec3 vBinormal;
in float vDistance;

layout (std140) uniform lightInfo
{
	vec3 vLight;
	vec4 lightColor;
	vec4 ambientColor;
	vec4 backColor;
};

uniform sampler2D diffuseSampler;
uniform sampler2D normalSampler;

out vec4 fragColor;

//const vec3 vLight = vec3(0.71, 0.71, 0.0);
const vec3 vIndirect = vec3(0.0, -1.0, 0.0);

void main()
{
	lowp vec4 diffuseTex = texture(diffuseSampler, vTexCoord);
	
	lowp vec4 normalTex = texture(normalSampler, vTexCoord);
	normalTex = normalize(normalTex * 2.0 - 1.0);
	mediump vec3 normal = vTangent.xyz * normalTex.x + vBinormal * normalTex.y + vNormal * normalTex.z;
	
	float diffuse = dot(normal, vLight);
	diffuse = clamp(diffuse, 0.0, 1.0);
	
	vec4 color = vec4(diffuse) * diffuseTex * lightColor;
	
	/*float indirect = dot(normal, vIndirect);
	indirect = clamp(indirect, 0.0, 1.0);
	vec4 indirectColor = vec4(0.2, 0.25, 0.07, 1.0) * indirect;
	
	color += indirectColor;*/
	
	vec4 ambient = diffuseTex * ambientColor;
	color += ambient;
	
	fragColor = mix(color, backColor, vDistance);
}