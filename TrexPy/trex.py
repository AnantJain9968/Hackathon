import streamlit
from datetime import timedelta
from random import randrange
import pandas as pd
import plotly.express as px 

def random_date(start, end):
    """
    This function will return a random datetime between two datetime 
    objects.
    """
    delta = end - start
    int_delta = (delta.days * 24 * 60 * 60) + delta.seconds
    random_second = randrange(int_delta)
    return start + timedelta(seconds=random_second)


## Set theme 
streamlit.set_page_config(page_title='Test case vizualization', page_icon=':smiley:', layout='wide', initial_sidebar_state='auto')


## Add title 
streamlit.title('Dashboard for test case vizualization')


## Upload CSV File 
file = streamlit.file_uploader("Upload CSV", type=['csv'])
## Read that file in pandas 
if file is not None:
    df = pd.read_csv(file)
    timestamps = []
    for index, elem in df.iterrows():
        timestamps.append(random_date(pd.to_datetime('2021-01-01'), pd.to_datetime('2024-12-31')))
    df['Timestamp'] = timestamps


    ## Multi select dropdown to select the Status 
    status = df['Status'].unique()
    status = list(status)
    status = tuple(status)
    selected = streamlit.multiselect('Select status', status)

    
    if len(selected) > 0:
        df = df[df['Status'].isin(selected)]


    ## Select Owner Category 
    owner = df['Category Owner'].unique()
    owner = list(owner)
    owner.insert(0, 'all')
    owner = tuple(owner)
    selected_owner = streamlit.selectbox('Select Owner', owner)

    ## Select Date range
    start_date = streamlit.date_input('Start date', pd.to_datetime('2021-01-01'))
    end_date = streamlit.date_input('End date', pd.to_datetime('2024-12-31'))


    if selected_owner != 'all':
        df = df[df['Category Owner'] == selected_owner]

    df = df[(df['Timestamp'] >= pd.to_datetime(start_date)) & (df['Timestamp'] <= pd.to_datetime(end_date))]

    ## Calculate week day from Timestamp
    df['Weekday'] = df['Timestamp'].dt.day_name()

    ## Visualize data in tabular format with filter on status column 
    streamlit.write('Test case data')
    streamlit.write(df, filter='all', width=1000, height=500, key=None)

    ## Pie chart of Frequency of Status 
    streamlit.write('Frequency of Status')
    ## Pie chart of Frequency of Status
    status_count = df['Status'].value_counts()
    streamlit.bar_chart(status_count)

    # Pie Chart of Status
    status_count = df['Status'].value_counts()
    temp_df = pd.DataFrame({'Status': status_count.index, 'Count': status_count.values})
    ## Pie chart using plotly
    fig = px.pie(temp_df, names='Status', values= 'Count' , title='Frequency of Status')
    streamlit.plotly_chart(fig)

    # Pie chart of owner distribution
    status_count = df['Category Owner'].value_counts()
    temp_df = pd.DataFrame({'Owner': status_count.index, 'Count': status_count.values})
    ## Pie chart using plotly
    fig = px.pie(temp_df, names='Owner', values= 'Count' , title='Frequency of Owner', width=1000, height=500)
    streamlit.plotly_chart(fig)


    ## Histogram of each Status for all days of week on x axis 
    streamlit.write('Histogram of each Status for all days of week on x axis')
    fig = px.histogram(df, x='Weekday', color='Status', width=1000, height=500)
    streamlit.plotly_chart(fig)

    ## Plot of each owner for all days on week on x axis with full width size
    streamlit.write('Plot of each owner for all days on week on x axis')
    fig = px.histogram(df, x='Weekday', color='Category Owner', width=1000, height=500)
    streamlit.plotly_chart(fig)

   