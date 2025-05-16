import os

def search_java_files(directory, output_html_file="java_MCache_search_results.html"):
    """
    Searches for 'GameUI' and lines containing 'ui.' in Java files
    within a directory and its subdirectories and outputs results to an HTML file.

    Args:
        directory (str): The path to the directory to start the search from.
        output_html_file (str): The name of the HTML file to write results to.
    """
    print(f"Searching for 'GameUI' and 'ui.' in Java files under: {directory}")
    print(f"Results will be saved to: {output_html_file}\n")

    try:
        # Open the HTML file for writing
        with open(output_html_file, 'w', encoding='utf-8') as html_file:
            # Write HTML header
            html_file.write("<!DOCTYPE html>\n")
            html_file.write("<html lang='en'>\n")
            html_file.write("<head>\n")
            html_file.write("  <meta charset='UTF-8'>\n")
            html_file.write("  <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n")
            html_file.write("  <title>Java UI Search Results</title>\n")
            html_file.write("  <style>\n")
            html_file.write("    body { font-family: sans-serif; line-height: 1.6; margin: 20px; }\n")
            html_file.write("    .file-section { margin-bottom: 20px; border: 1px solid #ccc; padding: 10px; border-radius: 5px; }\n")
            html_file.write("    .file-path { font-weight: bold; color: #333; margin-bottom: 5px; }\n")
            html_file.write("    .line { margin-left: 20px; white-space: pre-wrap; word-break: break-all; font-family: monospace; background-color: #f4f4f4; padding: 2px 5px; border-radius: 3px; }\n")
            html_file.write("  </style>\n")
            html_file.write("</head>\n")
            html_file.write("<body>\n")
            html_file.write("  <h1>Java UI Search Results</h1>\n")

            found_matches = False

            # Walk through the directory and its subdirectories
            for root, _, files in os.walk(directory):
                for file in files:
                    # Check if the file is a Java file
                    if file.endswith(".java"):
                        filepath = os.path.join(root, file)
                        print(f"Checking file: {filepath}")
                        file_has_matches = False
                        file_content = []

                        try:
                            # Open and read the file line by line
                            with open(filepath, 'r', encoding='utf-8') as f:
                                file_content = f.readlines()

                            for line_num, line in enumerate(file_content, 1):
                                # Check for "GameUI" or "ui."
                                if "MCache" in line:
                                    if not file_has_matches:
                                        # Start a new section for the file
                                        html_file.write(f"  <div class='file-section'>\n")
                                        html_file.write(f"    <div class='file-path'>File: {filepath}</div>\n")
                                        file_has_matches = True
                                        found_matches = True

                                    # Write the matching line to the HTML file
                                    # Escape HTML special characters for the line content
                                    escaped_line = line.strip().replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')
                                    html_file.write(f"    <div class='line'>Line {line_num}: {escaped_line}</div>\n")

                            if file_has_matches:
                                # Close the file section
                                html_file.write("  </div>\n")

                        except Exception as e:
                            print(f"  Error reading file {filepath}: {e}")
                            html_file.write(f"  <div class='file-section'>\n")
                            html_file.write(f"    <div class='file-path'>Error reading file: {filepath}</div>\n")
                            html_file.write(f"    <div class='line'>Error: {e}</div>\n")
                            html_file.write("  </div>\n")

            if not found_matches:
                 html_file.write("  <p>No matches found.</p>\n")


            # Write HTML footer
            html_file.write("</body>\n")
            html_file.write("</html>\n")

    except Exception as e:
        print(f"Error writing to HTML file {output_html_file}: {e}")


if __name__ == "__main__":
    # Get the directory where the script is located
    script_directory = os.path.dirname(os.path.abspath(__file__))

    # Run the search
    search_java_files(script_directory)

