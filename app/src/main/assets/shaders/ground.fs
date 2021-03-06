#version 300 es
precision highp float;

layout (std140) uniform lightInfo
{
	vec3 lightVector;
	vec4 lightColor;
	vec4 ambientColor;
	vec4 backColor;
	float shadowFactor;
};

uniform vec2 uFramebufferDimensions;

uniform sampler2DShadow shadowMapSampler;
uniform sampler2D reflectionSampler;
uniform sampler2D waterNormalSampler;

uniform sampler2D groundBlackSampler;
uniform sampler2D groundWhiteSampler;

uniform sampler2D groundBlackNormalSampler;

uniform int lod;

in vec4 vPosition;
in vec2 vTexCoord;
in vec3 vNormal;
in vec3 vTangent;
in vec3 vBinormal;
in vec3 vColor;
in vec3 vLight;

in float vDistance;
in float vDiffuse;
in vec4 vShadowCoords;

out vec4 fragColor;

void main()
{
	lowp vec4 groundNormalTex = texture(groundBlackNormalSampler, vTexCoord);
	lowp float groundHeight = groundNormalTex.w;
	groundNormalTex = normalize(groundNormalTex * 2.0 - 1.0);
	mediump vec3 normal = vTangent.xyz * groundNormalTex.x + vBinormal * -groundNormalTex.y + vNormal * groundNormalTex.z;

	lowp vec4 grassColor = texture(groundWhiteSampler, vTexCoord);
	lowp vec4 groundColor = texture(groundBlackSampler, vTexCoord) + 0.25;

	mediump vec4 normalTex = texture(waterNormalSampler, vTexCoord);
	normalTex = normalTex * 2.0 - 1.0;
	
	vec2 normalDistort = normalTex.xy * 0.025;
	
	vec2 screen = vec2(gl_FragCoord.x / uFramebufferDimensions.x, gl_FragCoord.y / uFramebufferDimensions.y);

	lowp vec4 reflectionColor = texture(reflectionSampler, screen + normalDistort);
	lowp vec4 diffuseColor = mix(groundColor, grassColor, vColor.x);
	
	lowp float shadow = textureProj(shadowMapSampler, vShadowCoords);
	shadow += ambientColor.x;
	shadow += shadowFactor;
	shadow = clamp(shadow, 0.0, 1.0);
	
	lowp float diffuse = dot(normal, lightVector);
	diffuse = clamp(diffuse, 0.0, 1.0);
	
	diffuse *= shadow;
	
	diffuseColor = (diffuseColor * lightColor * diffuse) + (diffuseColor * vec4(dot(normal, vLight) * 2.0 * shadowFactor));// + ambient;
	
	fragColor = mix(diffuseColor, backColor, vDistance);
	
	lowp float height = clamp(vColor.x + groundHeight, 0.0, 1.0);
	
	//fragColor = mix((fragColor + reflectionColor * (1.0 - height)) * 0.5, fragColor, height); 
	fragColor = mix(reflectionColor, fragColor, height);
}