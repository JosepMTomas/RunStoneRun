#version 300 es

in vec2 vTexCoord;
in vec3 vNormal;
in vec3 vTangent;
in vec3 vBinormal;
in float vDistance;
in vec3 vLight;

layout (std140) uniform lightInfo
{
	vec3 lightVector;
	vec4 lightColor;
	vec4 ambientColor;
	vec4 backColor;
	float shadowFactor;
};

uniform sampler2D diffuseSampler;
uniform sampler2D normalSampler;

out vec4 fragColor;

const vec3 vIndirect = vec3(0.0, -1.0, 0.0);

void main()
{
	lowp vec4 diffuseTex = texture(diffuseSampler, vTexCoord);
	
	lowp vec4 normalTex = texture(normalSampler, vTexCoord);
	normalTex = normalize(normalTex * 2.0 - 1.0);
	mediump vec3 normal = vTangent.xyz * normalTex.x + vBinormal * normalTex.y + vNormal * normalTex.z;
	
	float diffuse = dot(normal, lightVector);
	diffuse = clamp(diffuse, 0.0, 1.0);
	
	float diffuse2 = dot(normal, vLight);
	diffuse2 = clamp(diffuse2, 0.0, 1.0) * 2.0 * shadowFactor;
	
	vec4 color = (diffuseTex * diffuse * lightColor) + (diffuseTex * diffuse2);

	vec4 ambient = diffuseTex * ambientColor;
	color += ambient;
	
	fragColor = mix(color, backColor, vDistance);
}