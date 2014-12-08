#version 300 es
precision mediump float;

layout (location = 0) out float renderTarget0;	// View-Space Normalized Depth (R)
layout (location = 1) out vec4 renderTarget1;	// View-Space Normal (RGB) - Reflection Mask (A)
layout (location = 2) out vec4 renderTarget2;	// Diffuse albedo (RGB) - Roughness (A)
layout (location = 3) out vec4 renderTarget3;	// Eye->Pixel vector (RGB) - ShadowMask (A)

uniform samplerCube uSkyboxSampler;

in vec4 vPosition;
in vec3 vEye;
in float vDepth;


void main()
{
	vec4 diffuseAlbedo = texture(uSkyboxSampler, vPosition.xyz);
	//diffuseAlbedo = vec4(1.0);
	
	renderTarget0 = vDepth;
	renderTarget1 = vec4(1.0, 1.0, 1.0, 0.0);
	renderTarget2 = vec4(diffuseAlbedo.xyz, 0.0);
	renderTarget3 = vec4(vEye.xy, 1.0, 0.0);
}