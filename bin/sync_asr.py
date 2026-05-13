#!/usr/bin/env python

import os
import traceback
from os import environ
from os.path import join
from typing import Dict

import requests
from cmarkgfm import github_flavored_markdown_to_html
from cmarkgfm.cmark import Options as cmarkgfmOptions

envs: Dict[str, str] = { }
for key in ['CONFLUENCE_SUBDOMAIN', 'CONFLUENCE_USERNAME', 'CONFLUENCE_API_TOKEN',
            'GITHUB_WORKSPACE']:
    value = environ.get(key.upper())
    if not value:
        print(f'Missing value for {key}')
        exit(1)
    envs[key] = value

parent_id = '52232254'
auth = (envs['CONFLUENCE_USERNAME'], envs['CONFLUENCE_API_TOKEN'])
base_url = f"https://{envs['CONFLUENCE_SUBDOMAIN']}.atlassian.net/wiki/rest/api/content"

parent_response = requests.get(f"{base_url}/{parent_id}?expand=version,space",
                               auth=auth,
                               headers={ 'Accept': 'application/json' })
parent_response.raise_for_status()
parent_page = parent_response.json()
space_key = parent_page['space']['key']

cmark_options = cmarkgfmOptions.CMARK_OPT_NORMALIZE

try:
    with open(join(envs['GITHUB_WORKSPACE'], 'asr', 'index.md')) as f:
        index_content = f.read()

    index_html = github_flavored_markdown_to_html(index_content, cmark_options)
    new_parent_content = {
        'id': parent_page['id'],
        'type': parent_page['type'],
        'title': parent_page['title'],
        'version': { 'number': parent_page['version']['number'] + 1 },
        'body': {
            'editor': {
                'value': index_html,
                'representation': 'editor'
            }
        }
    }

    parent_update_resp = requests.put(
        f"{base_url}/{parent_id}",
        json=new_parent_content,
        auth=auth
    ).json()
    if 'statusCode' in parent_update_resp and parent_update_resp['statusCode'] >= 400:
        print(f"Failed to update parent page: {parent_update_resp}")
        exit(1)
    print(f"Uploaded content successfully to parent page {parent_id}")
except Exception:
    print("Failed to process index.md to parent page")
    traceback.print_exc()
    exit(1)

children_response = requests.get(f"{base_url}/{parent_id}/child/page?limit=200",
                                 auth=auth,
                                 headers={ 'Accept': 'application/json' })
children_response.raise_for_status()
children = children_response.json().get('results', [])
existing_pages = { child['title']: child for child in children }

asr_dir = join(envs['GITHUB_WORKSPACE'], 'asr')
files = [f for f in os.listdir(asr_dir) if f.endswith('.md') and f not in ('ASR-XX_template.md', 'index.md')]

for file_name in files:
    file_path = f"asr/{file_name}"
    print(f'Processing file {file_path}')

    try:
        with open(join(asr_dir, file_name)) as f:
            file_content = f.read()

        first_line = file_content.split('\n')[0].strip()
        title = first_line.lstrip('# ') if first_line.startswith('#') else file_name.replace('.md', '')

        html = github_flavored_markdown_to_html(file_content, cmark_options)

        if title in existing_pages:
            page_id = existing_pages[title]['id']
            page_url = f"{base_url}/{page_id}"
            detail_resp = requests.get(page_url, auth=auth, headers={ 'Accept': 'application/json' })
            detail_resp.raise_for_status()
            full_page = detail_resp.json()

            new_content = {
                'id': full_page['id'],
                'type': full_page['type'],
                'title': full_page['title'],
                'version': { 'number': full_page['version']['number'] + 1 },
                'body': {
                    'editor': {
                        'value': html,
                        'representation': 'editor'
                    }
                }
            }

            updated = requests.put(
                page_url,
                json=new_content,
                auth=auth
            ).json()
            if 'statusCode' in updated and updated['statusCode'] >= 400:
                print(f'Failed to update page {title}: {updated}')
                exit(1)
            link = updated['_links']['base'] + updated['_links']['webui']
            print(f'Uploaded content successfully to page {link}')
        else:
            new_content = {
                'type': 'page',
                'title': title,
                'space': { 'key': space_key },
                'ancestors': [{ 'id': parent_id }],
                'body': {
                    'editor': {
                        'value': html,
                        'representation': 'editor'
                    }
                }
            }
            created = requests.post(
                base_url,
                json=new_content,
                auth=auth
            ).json()
            if 'statusCode' in created and created['statusCode'] >= 400:
                print(f'Failed to create page {title}: {created}')
                exit(1)
            link = created.get('_links', { }).get('base', '') + created.get('_links', { }).get('webui', '')
            print(f'Created new page successfully: {link}')

    except Exception:
        print(f'Failed to process file {file_path}')
        traceback.print_exc()
        exit(1)
