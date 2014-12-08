#version 300 es
precision highp float;

//uniform vec2 uFramebufferDimensions;

uniform sampler2DShadow shadowMapSampler;
//uniform sampler2D reflectionSampler;
//uniform sampler2D waterNormalSampler;

uniform sampler2D groundBlackSampler;
//uniform sampler2D groundGraySampler;
uniform sampler2D groundWhiteSampler;

//uniform sampler2D groundBlackNormalSampler;

in vec4 vPosition;
in vec2 vTexCoord;
in vec3 vNormal;
in vec3 vTangent;
in vec3 vBinormal;
in vec3 vColor;

in float vDistance;
in vec4 vShadowCoords;

out vec4 fragColor;

void main()
{
	/*vec4 groundNormalTex = texture(groundBlackNormalSampler, vTexCoord);
	groundNormalTex = normalize(groundNormalTex * 2.0 - 1.0);
	vec3 normal = vTangent.xyz * groundNormalTex.x + vBinormal * -groundNormalTex.y + vNormal * groundNormalTex.z;*/

	vec4 grassColor = texture(groundWhiteSampler, vTexCoord);
	vec4 groundColor = texture(groundBlackSampler, vTexCoord);

	/*vec4 normalTex = texture(waterNormalSampler, vTexCoord);
	normalTex = normalTex * 2.0 - 1.0;
	
	vec2 normalDistort = normalTex.xy * 0.025;
	
	vec2 screen = vec2(gl_FragCoord.x / uFramebufferDimensions.x, gl_FragCoord.y / uFramebufferDimensions.y);

	fragColor = texture(reflectionSampler, screen + normalDistort) * vColor.x;*/
	fragColor = mix(groundColor, grassColor, clamp(pow(vColor.x, 2.0) * 2.0, 0.0, 1.0));
	
	
	float shadow = textureProj(shadowMapSampler, vShadowCoords);
	shadow += 0.25;
	shadow = clamp(shadow, 0.0, 1.0);
	
	/*float diffuse = dot(normal, normalize(vec3(0.5,1.0,0.0)));
	diffuse = clamp(diffuse, 0.0, 1.0);
	
	diffuse *= shadow;
	
	fragColor = fragColor * vec4(diffuse);
	
	fragColor = mix(fragColor, vec4(1.0), vDistance);
	fragColor = fragColor * (1.0 - vColor.y);*/
	
	fragColor = fragColor * shadow;
	fragColor = mix(fragColor, vec4(1.0), vDistance);
	
	///fragColor = mix(fragColor, vec4(1.0), vDistance);
	//fragColor = vec4(diffuse);
}