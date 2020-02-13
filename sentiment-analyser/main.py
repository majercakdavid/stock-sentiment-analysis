from flair.models import TextClassifier
from flair.data import Sentence

from json import dumps
from json import loads
from kafka import KafkaProducer, KafkaConsumer

classifier = TextClassifier.load('en-sentiment')

producer = KafkaProducer(bootstrap_servers=['broker:29092'],
                         value_serializer=lambda x: dumps(x).encode('utf-8'))

consumer = KafkaConsumer(
    'tweets-text',
     bootstrap_servers=['broker:29092'],
     auto_offset_reset='earliest',
     enable_auto_commit=True,
     value_deserializer=lambda x: loads(x.decode('utf-8')))

def getSentiment(text):
    print(f'Received text: {text}')
    sentence = Sentence(text)
    classifier.predict(sentence)
    # label is either POSITIVE or NEGATIVE for value property and double for score property
    label = sentence.labels[0]
    # set score to <-1,0) for NEGATIVE and (0,1> for POSITIVE
    label_score = (1 if label.value == "POSITIVE" else -1)*(label.score)
    # standardize values to be <0,0.5) for NEGATIVE and (0.5,1> for POSITIVE
    sentiment_value = ((label_score + 1)/2)
    print(f'Sentiment score: {sentiment_value}')
    return sentiment_value

for message in consumer:
    sentiment = getSentiment(message.value['text'])
    res = {
        "id": message.value['id'],
        "sentiment": sentiment
    }
    producer.send('tweets-sentiment', value=res)
