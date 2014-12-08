#version 300 es
precision highp float;

/*uniform sampler2D normalBuffer;
uniform sampler2D diffuseBuffer;
uniform sampler2D specularBuffer;
uniform sampler2D shadowBuffer;
uniform sampler2D depthBuffer;*/

uniform sampler2D depthSampler;
uniform sampler2D normalSampler;
uniform sampler2D diffuseSampler;
uniform sampler2D eyeSampler;

uniform mat4 inverseViewProjection;
uniform mat4 inverseProjection;

uniform int index;

in vec4 vPosition;
in vec2 vTexCoord;

layout (location = 0) out vec4 colorBuffer;
out vec4 fragColor;

vec3 lightPosition = vec3(50.0, 50.0, 0.0);

float LinearizeDepth(float z)
{
	float n = 0.1;
	float f = 1000.0;
	return (2.0 * n) / (f + n - z * (f - n));
}

void main()
{
	//fragColor = texture(normalBuffer, vTexCoord);
	//fragColor = fragColor * texture(diffuseBuffer, vTexCoord);
	//fragColor = vec4(0.5, 0.0, 0.0, 1.0);
	
	/*vec4 normalColor = texture(normalBuffer, vTexCoord);
	vec4 diffuseColor = texture(diffuseBuffer, vTexCoord);
	vec4 specularColor = texture(specularBuffer, vTexCoord);
	vec4 shadowColor = texture(shadowBuffer, vTexCoord);
	vec4 depthColor = texture(depthBuffer, vTexCoord);*/
	
	vec4 depthTexture = texture(depthSampler, vTexCoord);
	vec4 normalTexture = texture(normalSampler, vTexCoord);
	vec4 diffuseTexture = texture(diffuseSampler, vTexCoord);
	vec4 eyeTexture = texture(eyeSampler, vTexCoord);
	
	/*vec4 screenSpacePosition = vec4(vTexCoord * 2.0 - 1.0, depthColor.x * 2.0 - 1.0, 1.0);
	vec4 worldSpacePosition = inverseViewProjection * screenSpacePosition;
	vec3 finalPosition = worldSpacePosition.xyz / worldSpacePosition.w;*/
	
	//
	/*float pixelDepth = shadowColor.x * 2.0 - 1.0;
	vec4 worldPosition = inverseProjection * vec4(vTexCoord, pixelDepth, 1.0);
	worldPosition = vec4(worldPosition.xyz / worldPosition.w, 1.0);
	
	worldPosition = */
	
	/*vec3 vLight = normalize(lightPosition - finalPosition);
	float diffuse = dot(normalColor.xyz, vLight);
	diffuse = clamp(diffuse, 0.0, 1.0);*/
	
	//vec3 vLight = vec3(0.0, 1.0, 0.0);
	//float diffuse = dot(normalColor.xyz, vLight);
	//diffuse = clamp(diffuse, 0.0, 1.0);
	
	///////////////////////////////////////////////////////////////////
	
	vec3 vEye = eyeTexture.xyz * 2.0 - 1.0;
	vec3 P = eyeTexture.xyz * (depthTexture.x * 500.0);
	vec3 R = reflect(vEye, normalTexture.xyz);
	
	vec3 reflectionColor = vec3(0.0);
	
	float incr;
	float fi = 1.0;
	vec3 RP;
	vec3 IR;
	vec4 RPDepth;
	
	/*for(int i=1; i < 10; i++)
	{
		incr = 10.0 * fi;
		RP = P + (R * incr);
		IR = normalize(RP);
		
		RPDepth = texture(depthSampler, (IR.xy + 1.0) / 2.0);
		
		if(distance(IR * RPDepth.x, RP) < 10.0)
		{
			reflectionColor = texture(diffuseSampler, (IR.xy + 1.0)/2.0).xyz;
			break;
		}
		
		fi += 1.0;
	}*/
	
	reflectionColor = vEye * (depthTexture.x);
	
	///////////////////////////////////////////////////////////////////
	
	float depthColor = depthTexture.x / 500.0;
	
	if(index == 0) fragColor = vec4(depthColor);
	if(index == 1) fragColor = normalTexture;
	if(index == 2) fragColor = diffuseTexture;
	if(index == 3) fragColor = vec4(vEye, 0.0);
	if(index == 4) fragColor = vec4(reflectionColor, 0.0);
	if(index == 5) fragColor = vec4(0.0); //normalColor * diffuseColor * shadowColor;
	
	//colorBuffer = (normalColor * diffuseColor) + 0.2;
	//colorBuffer = diffuseColor * shadowColor;
	//colorBuffer.w = LinearizeDepth(texture(depthBuffer, vTexCoord).x);
	colorBuffer = vec4(0.0);
	
	//fragColor = vec4(vTexCoord, 0.0, 1.0);
}
