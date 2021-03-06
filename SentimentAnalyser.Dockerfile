FROM python:3.6-slim

RUN apt-get update && apt-get install -y unixodbc-dev gcc g++

RUN pip install torch==0.4.1
RUN pip install flair==0.4.5
RUN pip install spacy==2.0.11

RUN pip install \
    dateparser==0.7.0 \
    pymorphy2==0.8 \
    yargy==0.11.0 \
    natasha==0.10.0 \
    nltk==3.2.1 \
    yake==0.3.7 \
    python-dateutil==2.7.5 \
    kafka-python==2.0.0

RUN python -m nltk.downloader stopwords && python -m nltk.downloader punkt  && \
    python -m nltk.downloader averaged_perceptron_tagger

# RUN python -c 'import flair; _ = flair.models.SequenceTagger.load("ner-fast")'
RUN python -c 'from flair.models import TextClassifier; _ = TextClassifier.load("en-sentiment")'

RUN mkdir sentiment-analyser
ADD sentiment-analyser/main.py sentiment-analyser/main.py

WORKDIR /sentiment-analyser

ENTRYPOINT [ "python", "main.py" ]