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
	float shadowFactor;
};

uniform mat4 viewProjection;
uniform sampler2D diffuseSampler;

out vec4 vColor;

const vec3 lightPos = vec3(0.0, 50.0, 0.0);

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
	
	vec4 finalPosition = viewProjection * position;
	
	// diffuse component
	vec4 diffuseTex = texture(diffuseSampler, aTexCoord);
	float diffuse = dot(aNormal, vLight);
	float diffuse2 = dot(aNormal, normalize(lightPos - position.xyz));
	diffuse = clamp(diffuse, 0.0, 1.0);
	diffuse2 = clamp(diffuse2, 0.0, 1.0) * 2.0 * shadowFactor; 
	
	vec4 ambient = diffuseTex * ambientColor;
	
	diffuseTex = (diffuseTex * lightColor * diffuse) + (diffuseTex * diffuse2);
	
	vColor = mix(diffuseTex + ambient, backColor, property.w);
	
	
	//diffuse = dot(aNormal, normalize(vec3(0.0, 50.0, 0.0) - finalPosition.xyz));
	//diffuse = clamp(diffuse, 0.0, 1.0);
	
	//vColor += diffuse;
	
	// vertex shader output
	gl_Position = finalPosition;
}