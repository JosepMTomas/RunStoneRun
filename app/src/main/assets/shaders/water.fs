#version 300 es

uniform sampler2DShadow shadowMapSampler;
uniform sampler2D reflectionSampler;
uniform sampler2D waterNormalSampler;

uniform vec2 framebufferDimensions;

in vec4 vPosition;
in vec2 vTexCoord;
in vec3 vNormal;
in vec4 vTangent;
in vec3 vBinormal;

in float vDistance;

in vec4 vShadowCoords;

out vec4 fragColor;

const vec3 eyePos = vec3(0.0, 25.0, 50.0);
const vec3 vLight = normalize(vec3(0.0, 0.5, -1.0));

void main()
{
	vec2 screen = vec2(gl_FragCoord.x / framebufferDimensions.x, gl_FragCoord.y / framebufferDimensions.y);
	
	vec4 normalTex = texture(waterNormalSampler, vTexCoord);
	normalTex = normalTex * 2.0 - 1.0;
	
	//vec2 normalDistort = normalTex.xy * 0.025;
	vec2 normalDistort = normalTex.xy * 0.05;
	
	float shadow = textureProj(shadowMapSampler, vShadowCoords);
	shadow += 0.25;
	shadow = clamp(shadow, 0.0, 1.0);
	
	/*vec3 vEye = normalize(eyePos - vPosition.xyz);
	vec3 vReflected = reflect(vNormal, vLight);
	float specular = dot(vReflected, vEye);
	specular = clamp(specular, 0.0, 1.0);
	specular = pow(specular, 15.0);*/
	
	//fragColor = vec4(0.0,0.5,1.0,1.0);
	fragColor = texture(reflectionSampler, screen + normalDistort) * 0.75;
	//fragColor = fragColor + specular;
}