# Summer School Lab: Structural Methods for Handwriting Analysis

This Repository contains all information for the Lab of Andreas Fischer at the TC10/11 Summer School on Document Analysis and Recognition.

This Lab is organized by [Andreas Fischer](http://diuf.unifr.ch/main/diva/home/people/andreas-fischers-home-page) and [Marcel Würsch](http://diuf.unifr.ch/main/diva/home/people/marcel-w%C3%BCrsch)


## Goals

The goals of this Lab is twofold:
 - Interact with methods provided on DIVAServices in order to speed up your work
 - Perform a very basic Key Word Spotting with a structural approach based on graphs

## Data
The ground-truth data is available in the 'data' folder. 
This data is compiled from the following sources:
- [IAM Historical Document Database](http://www.fki.inf.unibe.ch/databases/iam-historical-document-database), for the line segmentation and the transcription
- [http://www.histograph.ch](http://www.histograph.ch), for the word segmentation

You find the following sub-folder in there:
### ground-truth
Contains ground-truth data.

#### transcription.txt
Contains the transcription of all words (on a character level) of the whole dataset. The Format is as follows:

	- XXX-YY-ZZ: XXX = Document Number, YY = Line Number, ZZ = Word Number
	- Contains the character-wise transcription of the word (letters seperated with dashes)
	- Special characters denoted with s_
		- numbers (s_x)
		- punctuation (s_pt, s_cm, ...)
		- strong s (s_s)
		- hyphen (s_mi)
		- semicolon (s_sq)
		- apostrophe (s_qt)
		- colon (s_qo)

#### locations
Contains bounding boxes for all words in the svg-format.

	- XXX.svg: File containing the bounding boxes for the given documents
	- **id** contains the same XXX-YY-ZZ naming as above

### images ###
Contains the original images in jpg-format.

## Tasks

This Lab is split into two bigger tasks with multiple smaller steps. These steps will be explained afterwards: 
- Preprocessing of the data
    - Binarize the original image
    - Cut out the words from the binarized page image
    - Extract word graphs from the word images 
- Spot a keyword
    - Match a keyword GXL gainst all word GXLs
        - Implement Hausdorff Edit Distance
    - Find top-10 matches
        - Display the top-10 images 

### Task 1 - Preprocessing of the data

Preprocessing of the data consists of two steps:
- Binarize the page image
- Cutting out the  words from the binarized page image
- Extracting the word graphs from the word images

These steps will be performed using methods provided on [DIVAServices](https://lunactic.github.io/DIVAServicesweb/), and to get you started you can find parts of the implementation of this in the [process.py](src/task1/process.py) file.

**Complete Binarization**
The method `binarize_page` should binarize the original input image using Otsu-Binarization from DIVAServices.
You can find a Tutorial on how to use methods on DIVAServices [here](https://lunactic.github.io/DIVAServicesweb/articles/first-execution/).

In order to execute the method you need to know the following pieces of information:
- the url of the method is: http://divaservices.unifr.ch/api/v2/binarization/otsubinarization/1
- the method takes no `parameters`
- the method takes the following `inputs`:
   -  `inputImage`: The reference to the image on DIVAServices in the form of `COLLECTION_NAME/FILENAME.EXTENSION`

Hints:
- Use the Tutorial, as well as the other existing methods that execute methods on DIVAServices for guidance.
- `pollResult(...)` can be used to automatically poll DIVAServices until the result is available.

**Complete Extract Words**
The methods `extract_words(...)` and `extract_words_binary(...)` extract the word images from the page. This is performed by cutting out the word images from an svg that provides the bounding boxes as a polygon for each word. Obviously we could perform this step offline, look for the according libraries etc., and do the implementation.

But the simpler way is to use a method that performs this on DIVAServices. So similarly as above complete the two methods using the following information:
- the url of the method is: http://divaservices.unifr.ch/api/v2/kws/wordimageextraction/1
- the method takes no parameters
- the method takes the following `inputs`:
    - `inputImage` : The reference to the binarized image on DIVAServices in the form of `COLLECTION_NAME/FILENAME.EXTENSION`
    - `pathSvg`: The reference to the path svg file on DIVAServices again in the form of `COLLECTION_NAME/FILENAME.EXTENSION`


**Run the preprocessing**
Once you have implemented all these steps, execute the method from the command line using `python src/process.py` from the root-directory of this project.

This will generate you the following outputs:
- `out/XXX/binary_page/`: a folder that contains the binarized page image
- `out/XXX/words_binary/`: a folder containing all binarized word images with the correct naming
- `out/XXX/graphs_binary/`: a folder containing all graph xml files with the correct naming

These can then be used for the second part of this tutorial.


### Task 2 - Comparing Keywords

### Running with Random HED
The basic provided implementation has a Hausdorff-Edit-Distance implementation that returns a random distance.

Test this implementation by changing the following variable in `Main.java`:
- `pathOut`: Full path to the `out` directory from Task 1

And compile and execute the Main file. This should generate you the following outputs:

```
Reading keyword graph and page graphs ...
... done. Read 3727 graphs in 6547.0 ms.

Matching keyword graph with page graphs ...
... done. Matched 3726 graphs in 6.0 ms.

Top 10
1. 271-34-06 (0.0)
2. 304-11-01 (0.001)
3. 273-32-05 (0.001)
4. 277-33-09 (0.001)
5. 274-34-06 (0.001)
6. 271-32-02 (0.001)
7. 300-04-01 (0.002)
8. 275-09-02 (0.002)
9. 300-29-03 (0.003)
10. 272-20-06 (0.003)
```

and the following image:

![top10_random](doc/top10_random.jpg)

### Computing HED
Obviously the above implementation is not very good, so we want to improve.
In the second task the goal is to compare a target keyword graph with all other graphs.
This should be perform using a Graph Edit Distance (GED) with Hausdorff-Edit-Distance (HED).

In `src/task2/kws` you find Java code that has an implementation to perform the matching on all extracted graph files.
All that is left to you to impement is the function
``` Java
public double match(KGraph g1, KGraph g2) {
}
```
In the file `HED.java`. Everything else is already implemented for you.

### Running recognition
Before compiling the sources make sure to change the two following variables in `Main.java`:
- `pathOut`: Full path to the `out` directory from Task 1
- `keywordId`: The ID of the keyword you want to use as search query

Compile your sources (best is using an IDE such as IntelliJ), or from the command line using
```
javac kws/*.java
```
from the `task2` directory.

And then execute the application using
```
java kws.Main
```
again from the `task2` directory. If everything runs fine on the console you should get logging information as follows:

```
Reading keyword graph and page graphs ...
... done. Read 3727 graphs in 20690.0 ms.

Matching keyword graph with page graphs ...
... done. Matched 3726 graphs in 2492.0 ms.

Top 10
1. 270-01-05 (0.0)
2. 276-02-05 (0.068)
3. 300-02-05 (0.069)
4. 274-01-05 (0.081)
5. 303-02-04 (0.081)
6. 271-02-04 (0.086)
7. 278-01-05 (0.087)
8. 275-01-04 (0.088)
9. 273-01-04 (0.091)
10. 302-01-05 (0.091)
```

And a window such as follows:

![top-10-figure](doc/top10.jpg)