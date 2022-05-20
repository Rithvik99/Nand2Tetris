// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

// Put your code here.

@bground
M=0 //default white color

(LOOP)

    @SCREEN //getting the address of 1st screen pixel
    D=A
    @pxl
    M=D

    @KBD //checking whether a key is pressed or not
    D=M
    @CHNGCLR
    D;JGT

    @bground //setting the color to white
    M=0
    @CLRSCRN //coloring the screen white
    0;JEQ

    (CHNGCLR) //setting the color to black
        @bground
        M=-1

    (CLRSCRN) //coloring the screen with the loaded color
        @bground 
        D=M
        @pxl //going to pixel address and coloring it
        A=M         
        M=D

        @pxl //moving to next pixel
        M=M+1
        D=M
            
        @KBD //checking if key is still pressed or not
        D=D-A
        @CLRSCRN
        D;JLT

@LOOP
0;JEQ //infinite loop for continous checking