package com.bennyplo.designgraphicswithopengl;

import android.opengl.GLES32;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

public class Sphere {
    private final String vertexShaderCode =
            "attribute vec3 aVertexPosition;"+//vertex of an object
                    " attribute vec4 aVertexColor;"+//the colour  of the object
                    "     uniform mat4 uMVPMatrix;"+//model view  projection matrix
                    "    varying vec4 vColor;"+//variable to be accessed by the fragment shader
                    "    void main() {" +
                    "        gl_Position = uMVPMatrix* vec4(aVertexPosition, 1.0);"+//calculate the position of the vertex
                    "        vColor=aVertexColor;}";//get the colour from the application program

    private final String fragmentShaderCode =
            "precision mediump float;"+ //define the precision of float
                    "varying vec4 vColor;"+ //variable from the vertex shader
                    "void main() {"+
                    "   gl_FragColor = vColor; }";//change the colour based on the variable from the vertex shader

    private final FloatBuffer vertexBuffer,colorBuffer;
    private final IntBuffer indexBuffer;
    private final int mProgram;
    private int mPositionHandle,mColorHandle;
    private int mMVPMatrixHandle;
    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static final int COLOR_PER_VERTEX = 4;
    private int vertexCount;// number of vertices
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private final int colorStride=COLOR_PER_VERTEX*4;//4 bytes per vertex
    float SphereVertex[] ={
    };
    static int SphereIndex[]={
    };
    static float SphereColor[]={
    };

    private void createSphere(float radius, int nolatitude, int nolongitude) {
        float vertices[] = new float[65535];
        int index[] = new int[65535];
        float color[] = new float[65535];
        int vertexIndex = 0;
        int colorIndex = 0;
        int indx = 0;
        float dist = 0;
        for (int row = 0; row <= nolatitude; row++) {
            double theta = row * Math.PI / nolatitude;
            double sinTheta = Math.sin(theta);
            double cosTheta = Math.cos(theta);
            float tcolor = -0.5f;
            float tcolorinc = 1/(float)(nolongitude+1);
            for (int col = 0; col <= nolongitude; col++) {
                double phi = col * 2 * Math.PI / nolongitude;
                double sinPhi = Math.sin(phi);
                double cosPhi = Math.cos(phi);
                double x = cosPhi * sinTheta;
                double y = cosTheta;
                double z = sinPhi * sinTheta;
                vertices[vertexIndex++] = (float)(radius * x);
                vertices[vertexIndex++] = (float)(radius * y) + dist;
                vertices[vertexIndex++] = (float)(radius * z);
                color[colorIndex++] = 1;
                color[colorIndex++] = Math.abs(tcolor);
                color[colorIndex++] = 0;
                color[colorIndex++] = 1;

                tcolor += tcolorinc;
            }
        }

        // index buffer
        for (int row = 0; row < nolatitude; row++) {
            for (int col = 0; col < nolongitude; col++) {
                int P0 = (row * (nolongitude + 1)) + col;
                int P1 = P0 + nolongitude + 1;
                index[indx++] = P0;
                index[indx++] = P1;
                index[indx++] = P0 + 1;
                index[indx++] = P1;
                index[indx++] = P1 + 1;
                index[indx++] = P0 + 1;
            }
        }

        SphereVertex = Arrays.copyOf(vertices, vertexIndex);
        SphereIndex = Arrays.copyOf(index, indx);
        SphereColor = Arrays.copyOf(color, colorIndex);
    }

    public Sphere() {
        createSphere(2, 30, 30);

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(SphereVertex.length * 4);// (# of coordinate values * 4 bytes per float)
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(SphereVertex);
        vertexBuffer.position(0);
        vertexCount=SphereVertex.length/COORDS_PER_VERTEX;
        ByteBuffer cb=ByteBuffer.allocateDirect(SphereColor.length * 4);// (# of coordinate values * 4 bytes per float)
        cb.order(ByteOrder.nativeOrder());
        colorBuffer = cb.asFloatBuffer();
        colorBuffer.put(SphereColor);
        colorBuffer.position(0);
        IntBuffer ib=IntBuffer.allocate(SphereIndex.length);
        indexBuffer=ib;
        indexBuffer.put(SphereIndex);
        indexBuffer.position(0);
        // prepare shaders and OpenGL program
        int vertexShader = MyRenderer.loadShader(GLES32.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyRenderer.loadShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderCode);
        mProgram = GLES32.glCreateProgram();             // create empty OpenGL Program
        GLES32.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES32.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES32.glLinkProgram(mProgram);                  // link the  OpenGL program to create an executable
        GLES32.glUseProgram(mProgram);// Add program to OpenGL environment
        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES32.glGetAttribLocation(mProgram, "aVertexPosition");
        // Enable a handle to the triangle vertices
        GLES32.glEnableVertexAttribArray(mPositionHandle);
        mColorHandle = GLES32.glGetAttribLocation(mProgram, "aVertexColor");
        // Enable a handle to the  colour
        GLES32.glEnableVertexAttribArray(mColorHandle);
        // Prepare the colour coordinate data
        GLES32.glVertexAttribPointer(mColorHandle, COLOR_PER_VERTEX, GLES32.GL_FLOAT, false, colorStride, colorBuffer);
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES32.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyRenderer.checkGlError("glGetUniformLocation");
    }

    public void draw(float[] mvpMatrix) {
        GLES32.glUseProgram(mProgram);//use the object's shading programs
        // Apply the projection and view transformation
        GLES32.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyRenderer.checkGlError("glUniformMatrix4fv");
        //set the attribute of the vertex to point to the vertex buffer
        GLES32.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES32.GL_FLOAT, false, vertexStride, vertexBuffer);
        GLES32.glVertexAttribPointer(mColorHandle, COORDS_PER_VERTEX,
                GLES32.GL_FLOAT, false, colorStride, colorBuffer);
//        GLES32.glDrawElements(GLES32.GL_LINES,SphereIndex.length,GLES32.GL_UNSIGNED_INT,indexBuffer);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES,SphereIndex.length,GLES32.GL_UNSIGNED_INT,indexBuffer);
    }
}
