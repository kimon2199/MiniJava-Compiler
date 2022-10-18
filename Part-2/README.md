To compile code run the `make` command in the current directory (make sure ... are present in the parent directory).

To run the executable use the command `java Main arg` where `arg` is a java program (like the ones found in `./examples`) to be converted into LLVM intermediate representation (IR) code.

Example: `java Main examples/Factorial.java`

Input File: `examples/Factorial.java`

```
class Factorial{
    public static void main(String[] a){
        System.out.println(new Fac().ComputeFac(10));
    }
}

class Fac {
    public int ComputeFac(int num){
        int num_aux ;
        if (num < 1)
            num_aux = 1 ;
        else
            num_aux = num * (this.ComputeFac(num-1)) ;
        return num_aux ;
    }
}

```

Output File: `examples/Factorial.ll` (notice that the output file is created inside the `examples/` folder)

```
@.Factorial_vtable = global [0 x i8*] []

@.Fac_vtable = global [1 x i8*] [
    i8* bitcast (i32 (i8*,i32)* @Fac.ComputeFac to i8*)
]

declare i8* @calloc(i32, i32)
declare i32 @printf(i8*, ...)
declare void @exit(i32)

@_cint = constant [4 x i8] c"%d\0a\00"
@_cOOB = constant [15 x i8] c"Out of bounds\0a\00"
@_cNSZ = constant [15 x i8] c"Negative size\0a\00"

define void @print_int(i32 %i) {
    %_str = bitcast [4 x i8]* @_cint to i8*
    call i32 (i8*, ...) @printf(i8* %_str, i32 %i)
    ret void
}

define void @throw_oob() {
    %_str = bitcast [15 x i8]* @_cOOB to i8*
    call i32 (i8*, ...) @printf(i8* %_str)
    call void @exit(i32 1)
    ret void
}

define void @throw_nsz() {
    %_str = bitcast [15 x i8]* @_cNSZ to i8*
    call i32 (i8*, ...) @printf(i8* %_str)
    call void @exit(i32 1)
    ret void
}

define i32 @main() {
    %_0 = call i8* @calloc(i32 1, i32 8)
    %_1 = bitcast i8* %_0 to i8***
    %_2 = getelementptr [1 x i8*], [1 x i8*]* @.Fac_vtable, i32 0, i32 0
    store i8** %_2, i8*** %_1
    %_3 = bitcast i8* %_0 to i8***
    %_4 = load i8**, i8*** %_3
    %_5 = getelementptr i8*, i8** %_4, i32 0
    %_6 = load i8*, i8** %_5
    %_7 = bitcast i8* %_6 to i32 (i8*,i32)*
    %_8 = call i32 %_7(i8* %_0,i32 10)
    call void (i32) @print_int(i32 %_8)
    ret i32 0
}

define i32 @Fac.ComputeFac(i8* %this, i32 %.num) {
    %num = alloca i32
    store i32 %.num, i32* %num
    %num_aux = alloca i32
    %_0 = load i32, i32* %num
    %_1 = icmp slt i32 %_0, 1
    %_2 = zext i1 %_1 to i8
    %_3 = trunc i8 %_2 to i1
    br i1 %_3, label %_lb0, label %_lb1
_lb0:
    store i32 1, i32* %num_aux
    br label %_lb2
_lb1:
    %_4 = load i32, i32* %num
    %_5 = bitcast i8* %this to i8***
    %_6 = load i8**, i8*** %_5
    %_7 = getelementptr i8*, i8** %_6, i32 0
    %_8 = load i8*, i8** %_7
    %_9 = bitcast i8* %_8 to i32 (i8*,i32)*
    %_10 = load i32, i32* %num
    %_11 = sub i32 %_10, 1
    %_12 = call i32 %_9(i8* %this,i32 %_11)
    %_13 = mul i32 %_4, %_12
    store i32 %_13, i32* %num_aux
    br label %_lb2
_lb2:
    %_14 = load i32, i32* %num_aux
    ret i32 %_14
}
```

