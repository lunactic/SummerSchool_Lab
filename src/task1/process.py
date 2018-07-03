from urllib.parse import urlparse

import json
import sys
import os
import time
import urllib.request
import base64
import requests


def main():
    """
    Entry point for the processing of page images into words and from words to word graphs

    This will perform the following steps:
        - extract words from the page using the svg with the word locations
        - upload all words as a collection to DIVAServices
        - extract a graph for each individual word that can be used for further graph based processing


    All the data used here is pre-stored on DIVAServices for easier processing.

    The collection names are:
     - kws_ss_orig: for the original input images
     - kws_ss_binary: for the original, binarized input images
     - kws_ss_locations: for the svg files with the word location information
     - kws_ss_words_XXX: for the word image collections for the individual words
     - kws_ss_words_binary_XXX: for the word image collections of the binarized individual words

    """

    image_numbers = ['270', '271', '272', '273', '274', '275',
                     '276', '277', '278', '279', '300', '301', '302', '303', '304']

    for image_number in image_numbers:
        print('processing image: ' + image_number)
        binarize_page(image_number)
        extract_words_binary(image_number)
        create_dir('out/'+image_number+'/graphs_binary/')
        collection_name = "kws_ss_words_binary"+image_number
        if not collection_exists(collection_name):
            for i, file in enumerate(os.listdir('out/'+image_number+'/words_binary/')):
                if i == 0:
                    create_collection(
                        'divaservices.unifr.ch/api/v2', 'out/'+image_number+'/words_binary/'+file, image_number, collection_name)
                else:
                    add_to_collection('divaservices.unifr.ch/api/v2/',
                                    collection_name, 'out/'+image_number+'/words_binary/'+file)

            extract_word_graphs_binary(collection_name, image_number)


def binarize_page(input_image):
    """
    Binarize a page using simple otsu-binarization on DIVAServices

    Arguments:
        input_image {string} -- The page number
    """

    # TODO: Execute the method on DIVAServices
    # - store the result from poll_result(...) in `result`

    outputFiles = result['output']

    files = [x['file'] for x in outputFiles if x['file']
             if x['file']['mime-type'] == 'image/jpeg']

    create_dir('out/'+input_image+'/binary_page/')

    # download the word images
    for file in files:
        urllib.request.urlretrieve(
            file['url'], 'out/'+input_image+'/binary_page/'+input_image+'_binary.jpg')


def extract_words_binary(input_image):
    """
    Extract word images from binary input images

    Arguments:
        input_image {string} -- the page number
    """
    print('extracting words from binarized page: ' + input_image)

    # TODO: Execute the method on DIVAServices
    # - store the result from poll_result(...) in `result`

    outputFiles = result['output']

    pngFiles = [x['file'] for x in outputFiles if x['file']
                if x['file']['mime-type'] == 'image/png']

    create_dir('out/'+input_image+'/words_binary/')

    # store the resulting word images
    for file in pngFiles:
        urllib.request.urlretrieve(
            file['url'], 'out/'+input_image+'/words_binary/'+file['name'])


def extract_word_graphs_binary(word_collection, input_image):
    """
    Extract word graphs from a collection of binarized word images

    Arguments:
        word_collection {string} -- the name of the word image collection on DIVAServices
        input_image {string} -- The document number
    """

    print('extracting word graphs from binarized page: ' + input_image)

    # Perform the execution on DIVAServices
    url = "http://divaservices.unifr.ch/api/v2/graph/imagetograph/1"

    payload = {"parameters": {}, "data": [
        {"inputImage": word_collection+"/*"}]}
    headers = {'content-type': 'application/json'}

    response = json.loads(requests.request(
        "POST", url, data=json.dumps(payload), headers=headers).text)
    print(response['results'])
    resultLinks = [x for x in response['results']]

    # download the individual results
    for i, resultLink in enumerate(resultLinks):
        result = poll_result(resultLink['resultLink'])
        outputFiles = result['output']
        xmlFiles = [x['file'] for x in outputFiles if x['file']
                    if x['file']['mime-type'] == 'application/xml']
        # download the gxl file
        for file in xmlFiles:
            filename = os.path.basename(urlparse(file['url']).path)
            urllib.request.urlretrieve(
                file['url'], 'out/'+input_image+'/graphs_binary/'+filename)


def create_collection(base_url, input_image, image_number, collection_name):
    """Uploads an image to DIVAServices

    Arguments:
        input_image {string} -- The path to the input image to use

    Returns:
        string -- the DIVAServices identifier of the uploaded image
    """

    url = "http://" + base_url + "/collections"
    with open(input_image, "rb") as image_file:
        encoded_string = base64.b64encode(
            image_file.read()).decode('ascii')
        file_name = os.path.basename(input_image)
        payload = {"name": collection_name,
                   "files": [{"type": "image",
                              "value": encoded_string,
                              "name": file_name}]}
        headers = {
            'content-type': "application/json"
        }
        response = json.loads(requests.request(
            "POST", url, data=json.dumps(payload), headers=headers).text)
        return response['collection']


def add_to_collection(base_url, collection_name, input_image):
    """adds an image to a DIVAServices collection

    Arguments:
        input_image {string} -- The path to the input image to use

    Returns:
        string -- the DIVAServices identifier of the uploaded image
    """

    url = "http://" + base_url + "/collections/" + collection_name
    with open(input_image, "rb") as image_file:
        encoded_string = base64.b64encode(
            image_file.read()).decode('ascii')
        file_name = os.path.basename(input_image)
        payload = {"files": [{"type": "image",
                              "value": encoded_string,
                              "name": file_name}]}
        headers = {
            'content-type': "application/json"
        }
        requests.request("PUT", url, data=json.dumps(payload), headers=headers)


def create_dir(dir_path):
    """
    Create a directory on the harddrive

    Arguments:
        dir_path {string} -- the path of the directory to create
    """

    if not os.path.isdir(dir_path):
        os.makedirs(dir_path)


def poll_result(result_link):
    """ 
    Polls for the result of the execution in 1s intervals

    Arguments:
        result_link {string} -- the resultLink generated by the POST request that started the execution

    Returns:
        [json] -- the result of the execution
    """

    response = json.loads(requests.request("GET", result_link).text)
    while(response['status'] != 'done'):
        if(response['status'] == 'error'):
            sys.stderr.write(
                'Error in executing the request. See the log file at: ' + response['output'][0]['file']['url'])
            sys.exit()
        time.sleep(1)
        response = json.loads(requests.request("GET", result_link).text)
    return response


def collection_exists(collection_name):
    """
    Check if a collection already exists on DIVAServices

    Arguments:
        collection_name {string} -- the name of the collection on DIVAServices

    Returns:
        bool -- True if the collection exists, False otherwise
    """

    url = "http://divaservices.unifr.ch/api/v2/collections/" + collection_name
    payload = ""
    response = json.loads(requests.request("GET", url, data=payload).text)
    return 'statusCode' in response and response['statusCode'] == 200


if __name__ == "__main__":
    main()
