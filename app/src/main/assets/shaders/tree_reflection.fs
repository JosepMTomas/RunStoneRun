#version 300 es
precision lowp float;

layout (std140) uniform lightInfo
{
	vec3 vLight;
	vec4 lightColor;
	vec4 ambientColor;
};

uniform sampler2D diffuseSampler;

in vec2 vTexCoord;
in float vDistance;

out vec4 fragColor;

void main()
{
	vec4 diffuseTex = texture(diffuseSampler, vTexCoord);

	if(diffuseTex.w < 0.6) discard;
	
	fragColor = mix(diffuseTex * lightColor, vec4(1.0), vDistance);
}