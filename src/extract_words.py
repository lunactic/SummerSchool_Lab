import sys
from PIL import Image, ImageDraw
from svgpathtools import svg2paths, parser
import numpy


def main():
    # gets list of words for each image (each word is a numpy array)
    binary_image = sys.argv[1]
    svg_image = sys.argv[2]

    cropImage(binary_image, svg_image)


def cropImage(binary_image, svg_image):
    polygons, ids = getPolygonsFromSVGPaths(svg_image)
    words = []
    for i in range(len(polygons)):
        words.append(cropWord(polygons[i],ids[i], binary_image, i))
    return words


def getPolygonsFromSVGPaths(svg_image):
    paths, attributes = svg2paths(svg_image)
    polygons = []
    ids = []
    for k, v in enumerate(attributes):
        d = v['d']
        # remove letters
        d = d.replace("M", "")
        d = d.replace("L", "")
        d = d.replace("Z", "")
        it = iter([float(coord) for coord in d.split()])  # convert to floats
        polygon = list(zip(it, it))  # create polygon
        polygons.append(polygon)
        ids.append(v['id'])
        print(v['id'])
    return (polygons,ids)


def cropWord(polygon,word_name, binary_image, imgNumber):
    # inspired by https://stackoverflow.com/questions/22588074/polygon-crop-clip-using-python-pil

    # read image as RGB and add alpha (transparency)
    im = Image.open(binary_image).convert("RGBA")

    # convert to numpy (for convenience)
    imArray = numpy.asarray(im)

    # create mask
    maskIm = Image.new('L', (imArray.shape[1], imArray.shape[0]), 0)
    ImageDraw.Draw(maskIm).polygon(polygon, outline=1, fill=1)
    mask = numpy.array(maskIm)

    # assemble new image (uint8: 0-255)
    newImArray = numpy.empty(imArray.shape, dtype='uint8')

    # colors (three first columns, RGB)
    newImArray[:, :, :3] = imArray[:, :, :3]

    # transparency (4th column)
    newImArray[:, :, 3] = mask * 255

    # back to Image from numpy
    newIm = Image.fromarray(newImArray, "RGBA")

    image_data = numpy.asarray(newIm)
    image_data_bw = image_data[:, :, 3]
    non_empty_columns = numpy.where(image_data_bw.max(axis=0) > 0)[0]
    non_empty_rows = numpy.where(image_data_bw.max(axis=1) > 0)[0]
    cropBox = (min(non_empty_rows), max(non_empty_rows),
               min(non_empty_columns), max(non_empty_columns))

    image_data_new = image_data[cropBox[0]:cropBox[
        1] + 1, cropBox[2]:cropBox[3] + 1, :]

    new_image = Image.fromarray(image_data_new)

    new_image.save(sys.argv[3] + "/crops/" + word_name + ".png")

    return newImArray


if __name__ == "__main__":
    main()
